package com.github.phalexei.dismake.parser;

import com.github.phalexei.dismake.Main;
import com.github.phalexei.dismake.work.Target;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Makefile parser.
 *
 * Reads a Makefile and converts it into a bunch of {@link Target} instances.
 */
public class Parser {

	public static class DependencyNotFoundException extends Throwable {
		public DependencyNotFoundException(String s) {
			super(s);
		}
	}

	public static Map<String, Target> parse(String fileName) throws IOException, DependencyNotFoundException {
		Map<String, Target> targets = readTargets(fileName);
		populateDependencies(targets);
		return targets;
	}

	private static void populateDependencies(Map<String, Target> targets) throws DependencyNotFoundException {
		if (targets != null) {
			List<String> weakDependencies;
			Target trueDependency;
			int nbTarget = targets.size();
			int i = 0;
			for (Target target : targets.values()) {
				System.out.println("Populating target n°" + i++ + "/" + nbTarget);
				weakDependencies = target.getWeakDependencies();
				for (String weakDependency : weakDependencies) {
					trueDependency = targets.get(weakDependency);
					if (trueDependency != null) {
						target.addDependency(trueDependency, false);
					} else {
						// dependency might be a file in current dir, search for it
						File folder = new File(".");
						boolean found = false;
						for (File f : folder.listFiles()) {
							if (f.isFile() && f.getName().equals(weakDependency)) {
								List<String> dependencies = new ArrayList<>();
								dependencies.add(f.getName());
								target.addDependency(new Target("", weakDependency, dependencies), true);
								found = true;
							}
						}
						if (!found) {
							throw new DependencyNotFoundException("Target: '" + target.getName() + "'; Dependency not found: " + weakDependency);
						}
					}
				}
				target.cleanWeakDependencies();
			}
		}
	}

	private static Map<String, Target> readTargets(String fileName) throws IOException {
		Map<String, Target> targets = new HashMap<>();
		List<String> dependencies = new ArrayList<>();

		List<String> fileContents = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);

		String currentLine, target, command;
		boolean firstTarget = true;

		for (int i = 0; i < fileContents.size(); i++) {
			currentLine = fileContents.get(i).replaceAll("\t", "");

			if (i % 20 == 0) {
				System.out.println("read target n°" + i);
			}

			// Ignore empty lines
			if (!currentLine.isEmpty() && !currentLine.startsWith("#")) {
				String split[] = currentLine.split(":");
				for (int j = 0; j < split.length; j++) {
					split[j] = split[j].trim();
				}
				target = split[0];

                // Check for and register dependencies
				if (split.length > 1) {
					for (String dep : split[1].split(" ")) {
						if (dep != null && !dep.isEmpty() && !dep.equals(" ")) {
							dependencies.add(dep);
						}
					}
				}

				// Read the command associated with the target, if any
				currentLine = fileContents.get(++i).replaceAll("\t", "").trim();
				command = currentLine.isEmpty() ? null : currentLine;

				// Register target with weak dependencies. Dependencies will be checked later.
				targets.put(target, new Target(command, target, dependencies));
				if (firstTarget && !target.startsWith(".")) {
					firstTarget = false;
					targets.put(Main.FIRST, targets.get(target));
				}
				dependencies.clear();
			}
		}

		return targets;
	}
}
