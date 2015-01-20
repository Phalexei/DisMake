package com.github.phalexei.dismake.parser;

import com.github.phalexei.dismake.Main;
import com.github.phalexei.dismake.Target;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/*
 * Parser
 *
 * Parser for a makefile
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
			for (Target target : targets.values()) {
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
							throw new DependencyNotFoundException("Target : " + target.getName() + "\nDependency not found : " + weakDependency);
						}
					}
				}
				target.cleanWeakDependencies();
			}
		}
	}

	private static Map<String, Target> readTargets(String fileName) throws IOException {
		String target;
		String command;
		Map<String, Target> targets = new HashMap<>();
		List<String> dependencies = new ArrayList<>();

		List<String> fileContents = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);

		String currentLine;
		boolean firstTarget = true;

		for (int index = 0; index < fileContents.size(); index++) {
			currentLine = fileContents.get(index).replaceAll("\t", "");

			if (!currentLine.isEmpty() && !currentLine.startsWith("#")) { // to verifiy if there's something to do
				String words[] = currentLine.split(":");
				for (int i = 0; i < words.length; i++) {
					words[i] = words[i].trim();
				}
				target = words[0];

				if (words.length > 1) { // if there are some dependencies
					for (String dep : words[1].split(" ")) {
						if (dep != null && !dep.isEmpty() && !dep.equals(" ")) {
							dependencies.add(dep);
						}
					}
				}

				//read the command associated with the target
				currentLine = fileContents.get(++index).replaceAll("\t", "");
				command = currentLine;

				// add the things to the main list
				// for now dep has just a name, it's incomplete
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
