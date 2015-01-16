package com.github.phalexei.dismake.work;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Result implements Serializable {
    private final String taskName;
    private final byte[] file;
    private final String stdOut;
    private final String stdErr;
    private final int exitCode;

    public Result(Task task, String stdOut, String stdErr, int exitCode) throws IOException {
        taskName = task.getTarget().getName();
        this.file = Files.readAllBytes(Paths.get(taskName));

        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.exitCode = exitCode;
    }

    public String getStdOut() {
        return stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public byte[] getFile() {
        return file;
    }
}
