package com.github.phalexei.dismake.work;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * A task created by the server and sent to clients
 */
public class Task implements Serializable {

    /**
     * Target of this task
     */
    private Target target;

    /**
     * Dependencies of this task
     */
    private Map<String, byte[]> files;

    /**
     * Creates a new task
     *
     * @param target target of this task
     * @throws IOException if a dependency can't be read
     */
    public Task(Target target) throws IOException {
        this.target = target;
        this.files = new HashMap<>();
        for (Target t : target.getDependencies().values()) {
            this.files.put(t.getName(), Files.readAllBytes(Paths.get(t.getName())));
        }
    }

    /**
     * Gets this task's target
     *
     * @return this task's target
     */
    public Target getTarget() {
        return this.target;
    }

    /**
     * Gets this task's dependencies
     *
     * @return this task's dependencies
     */
    public Map<String, byte[]> getFiles() {
        return this.files;
    }
}
