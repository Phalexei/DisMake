package com.github.phalexei.dismake.work;

import com.github.phalexei.dismake.Target;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public Task(Target target, Path... files) throws IOException {
        this.type = TaskType.WORK;
        this.target = target;
        this.files = new byte[files.length][];
        int i = 0;
        for (Path path : files) {
            this.files[i++] = Files.readAllBytes(path);
        }
    }

    public Target getTarget() {
        return this.target;
    }

    public TaskType getType() {
        return this.type;
    }
}
