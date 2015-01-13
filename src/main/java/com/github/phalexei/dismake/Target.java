package com.github.phalexei.dismake;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Target
 *
 * Our structure
 */
public class Target {
	private String name;
	private Map<String, Target> dependencies;
	private List<String> weakDependencies;
	private String command;

	public Target(String command, String name, List<String> dependencies) {
		this.name = name;
		this.weakDependencies = new ArrayList<>(dependencies);
		this.command = command;
		this.dependencies = new HashMap<>();
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

	@Override
	public boolean equals(Object o) {
		return o instanceof Target && ((Target) o).name.equals(this.name);
	}

	public String getName() {
		return name;
	}
	
	public Target getDependency(String name) {
		return dependencies.get(name);
	}

	public List<String> getWeakDependencies() {
		return weakDependencies;
	}

	public void addDependency(Target trueDependency) {
		dependencies.put(trueDependency.name, trueDependency);
	}

	public void cleanWeakDependencies() {
		weakDependencies.clear();
	}
}
