package com.github.phalexei.dismake.work;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class Result implements Serializable {
    private final String taskName;
    private final byte[] file;

    public Result(Task task, Path file) throws IOException {
        taskName = task.getTarget().getName();
        this.file = Files.readAllBytes(file);
    }

    public String getTaskName() {
        return this.taskName;
    }

    public byte[] getFile() {
        return file;
    }
}
