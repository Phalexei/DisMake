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

	public static void parse(String fileName) throws IOException, DependencyNotFoundException {
		Map<String, Target> targets = readTargets(fileName);
		populateDependencies(targets);
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
						target.addDependency(trueDependency);
					} else {
						throw new DependencyNotFoundException();
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

		int index = 0;
		String currentLine = fileContents.get(index++);

		while (currentLine != null) {
			if (!currentLine.isEmpty()) { // to verifiy if there's something to do
				String words[] = currentLine.split(":");
				target = words[0];

				if (words.length > 1) { // if there are some dependencies
					Collections.addAll(dependencies, words[1].split(" "));
				}

				//read the command associated with the target
				currentLine = fileContents.get(index++);
				command = currentLine;

				// add the things to the main list
				// for now dep has just a name, it's incomplete
				targets.put(target, new Target(command, target, dependencies));
			}
			// advance to next line
			currentLine = fileContents.get(index++);
		}

		return targets;
	}

	private static class DependencyNotFoundException extends Throwable {
	}
}
