package com.github.phalexei.dismake.server;

import com.github.phalexei.dismake.Main;
import com.github.phalexei.dismake.parser.Parser;
import com.github.phalexei.dismake.parser.Parser.DependencyNotFoundException;
import com.github.phalexei.dismake.work.Result;
import com.github.phalexei.dismake.work.Target;
import com.github.phalexei.dismake.work.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RmiServerImpl extends UnicastRemoteObject implements RmiServer {

    public static class MainTargetNotFoundException extends Throwable {
        public MainTargetNotFoundException(String s) {
            super(s);
        }
    }

    private final Queue<Task>         tasks;
    private final Map<String, Target> lockedTasks;
    private final Object              clientsLock;
    private       boolean             parsingDone;

    public RmiServerImpl(String url, String fileName, String theTarget) throws IOException, DependencyNotFoundException, MainTargetNotFoundException {
        super(0);    // required to avoid the 'rmic' step
        this.parsingDone = false;
        Main.print("RMI server started on " + url);

        try { //special exception handler for registry creation
            LocateRegistry.createRegistry(1099);
            Main.print("java RMI registry created.");
        } catch (RemoteException e) {
            //do nothing, error means registry already exists
            Main.print("java RMI registry already exists.");
        }

        clientsLock = new Object();
        // Bind this object instance to the name "RmiServer"
        Naming.rebind("//" + url + "/RmiServer", this);
        Main.print("PeerServer bound in registry");

        Map<String, Target> map = Parser.parse(fileName);

        Target mainTarget;
        if (theTarget != null) {
            mainTarget = map.get(theTarget);
        } else {
            mainTarget = map.get(Main.FIRST);
        }

        if (mainTarget != null) {
            lockedTasks = new ConcurrentHashMap<>();
            tasks = new ConcurrentLinkedQueue<>();
            if (mainTarget.getDependencies().size() > 0) {
                lockedTasks.put(mainTarget.getName(), mainTarget);
                for (Target t : map.values()) {
                    if (mainTarget.dependsOn(t.getName())) {
                        if (!t.available()) {
                            lockedTasks.put(t.getName(), t);
                        } else {
                            tasks.add(new Task(t));
                        }
                    }
                }
            } else {
                tasks.add(new Task(mainTarget));
            }
        } else {
            throw new MainTargetNotFoundException("Target : " + theTarget + "not found in " + fileName);
        }
        this.parsingDone = true;
        synchronized (this.clientsLock) {
            this.clientsLock.notifyAll();
        }
    }

    @Override
    public Task getTask() throws RemoteException {
        synchronized (this.clientsLock) {
            Task task;
            while (!this.parsingDone) {
                try {
                    clientsLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (tasks.size() > 0) {
                task = tasks.poll();
            } else if (lockedTasks.size() > 0) { // no task available right now, but in the future there will be
                try {
                    clientsLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                task =  tasks.size() > 0 ? tasks.poll() : null;
            } else { // nothing more to do
                task =  null;
            }

            if (task != null && task.getTarget().getCommand() == null) {
                onTaskSuccess(task.getTarget().getName(), null);
                task = getTask();
            }
            return task;
        }
    }

    @Override
    public void sendResults(Result result) {
        if (result.getExitCode() == 0) { // exit code for "success"
            onTaskSuccess(result.getFileName(), result.getFileContent());
        } else { // failure during build
            onTaskFailure(result.getFileName(), result.getExitCode(), result.getStdErr());
        }
    }

    @Override
    public void errorOnTask(Task failedTask) throws RemoteException {
        tasks.add(failedTask);
    }

    private void onTaskFailure(String taskName, int exitCode, String stdErr) {
        Main.print(taskName + " failed with error code: " + exitCode);
        Main.print("More information:");
        Main.print(stdErr);
        System.exit(exitCode);
    }

    private void onTaskSuccess(String fileName, byte[] fileContent) {
        try {
            if (fileContent != null) {
                Files.write(Paths.get(fileName), fileContent);
                Main.print("Copied result file " + fileName + " from client");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        synchronized (clientsLock) {
            for (Target t : lockedTasks.values()) {
                if (t.getDependencies().containsKey(fileName)) {
                    t.resolveOneDependency();
                    if (t.available()) {
                        try {
                            tasks.add(new Task(t));
                            lockedTasks.remove(t.getName());
                            clientsLock.notify();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (tasks.size() == 0 && lockedTasks.size() == 0) { // no more tasks, wake every hanging process
                clientsLock.notifyAll();
                Main.print("DisMake terminated successfully :-)");
                Main.print("Server shutting down.");
                System.exit(0);
            }
        }
    }
}
