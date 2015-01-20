package com.github.phalexei.dismake.work;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Result of a task
 */
public class Result implements Serializable {
    private final String fileName;
    private final byte[] fileContent;
    private final String stdOut;
    private final String stdErr;
    private final int    exitCode;

    public Result(Task task, String stdOut, String stdErr, int exitCode) throws IOException {
        fileName = task.getTarget().getName();
        Path path = Paths.get(fileName);
        if (path != null && Files.exists(path)) {
            this.fileContent = Files.readAllBytes(path);
        } else {
            this.fileContent = null;
        }

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

    public String getFileName() {
        return this.fileName;
    }

    public byte[] getFileContent() {
        return fileContent;
    }
}
