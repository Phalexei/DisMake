package com.github.phalexei.dismake.work;

import com.github.phalexei.dismake.Target;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A task
 * //TODO
 */
public class Task implements Serializable {

    private TaskType type;
    private Target target;
    private byte[][] files;

    public Task(TaskType type) {
        this.type = type;
        this.target = null;
        this.files = null;
    }

    public Task(Target target) throws IOException {
        this.type = TaskType.WORK;
        this.target = target;
        this.files = new byte[target.getDependencies().size()][];
        int i = 0;
        for (Target t : target.getDependencies().values()) {
            this.files[i++] = Files.readAllBytes(Paths.get(t.getName()));
        }
    }

    public Target getTarget() {
        return this.target;
    }

    public TaskType getType() {
        return this.type;
    }

    public byte[][] getFiles() {
        return files;
    }
}
