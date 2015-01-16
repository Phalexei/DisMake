package com.github.phalexei.dismake;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Target
 *
 * Our structure
 */
public class Target implements Serializable {
	private String name;
	private Map<String, Target> dependencies;
	private List<String> weakDependencies;
	private String command;
	private int dependenciesCounter;

	public Target(String command, String name, List<String> dependencies) {
		this.name = name;
		this.weakDependencies = new ArrayList<>(dependencies);
		this.dependenciesCounter = 0;
		this.command = command;
		this.dependencies = new HashMap<>();
	}

	public void cleanWeakDependencies() {
		weakDependencies.clear();
	}

	public void addDependency(Target dependency, boolean file) {
		dependencies.put(dependency.name, dependency);
		if (!file) {
			++this.dependenciesCounter;
		}
	}

	public void resolveOneDependency() {
		--dependenciesCounter;
	}

	/**
	 * @return true if this Target is ready (i.e. all its dependencies are
	 * resolved)
	 */
	public boolean available() {
		return dependenciesCounter == 0;
	}

	public boolean dependsOn(String target) {
		if (dependencies.containsKey(target)) {
			return true;
		} else {
			boolean found = false;
			for (Target t : dependencies.values()) {
				if (t.dependsOn(target)) {
					found = true;
					break;
				}
			}
			return found;
		}
	}

	public String getName() {
		return name;
	}

	/**
	 * @return the list of weak (not yet verified) dependencies of a
	 * {@link com.github.phalexei.dismake.Target}
	 */
	public List<String> getWeakDependencies() {
		return weakDependencies;
	}

	public Map<String, Target> getDependencies() {
		return dependencies;
	}

	public String getCommand() {
		return command;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Target && ((Target) o).name.equals(this.name);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(name).append(": ");
		if (dependencies != null) {
			for (Target c : dependencies.values()) {
				result.append(c.getName()).append(" ");
			}
		}
		result.append("\n").append(command).append("\n").append("\n");
		return result.toString();
	}
}
