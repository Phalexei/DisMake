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
		this.dependenciesCounter = dependencies.size();
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

	public List<String> getWeakDependencies() {
		return weakDependencies;
	}

	public void addDependency(Target trueDependency) {
		dependencies.put(trueDependency.name, trueDependency);
	}

	public void cleanWeakDependencies() {
		weakDependencies.clear();
	}

	public Map<String, Target> getDependencies() {
		return dependencies;
	}

	public void resolveOneDependency() {
		dependenciesCounter--;
	}

	public boolean available() {
		return dependenciesCounter == 0;
	}
}
