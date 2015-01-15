package com.github.phalexei.dismake.work;

public class Result {
    private final String taskName;

    public Result(Task task) {
        taskName = task.getTarget().getName();
    }

    public String getTaskName() {
        return this.taskName;
    }
}
