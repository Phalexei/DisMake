package com.github.phalexei.dismake.work;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class Result implements Serializable {
    private final String taskName;
    private final byte[][] files;

    public Result(Task task, Path... files) throws IOException {
        taskName = task.getTarget().getName();
        this.files = new byte[files.length][];
        int i = 0;
        for (Path path : files) {
            this.files[i++] = Files.readAllBytes(path);
        }
    }

    public String getTaskName() {
        return this.taskName;
    }
}
