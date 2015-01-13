package com.github.phalexei.dismake.work;

import com.github.phalexei.dismake.Target;

import java.io.Serializable;

/**
 * A task
 * //TODO
 */
public class Task implements Serializable {

    private Target target;

    public Task(Target target) {
        //TODO
        this.target = target;
    }

    public Target getTarget() {
        return this.target;
    }
}
