package com.github.phalexei.dismake.server;

import com.github.phalexei.dismake.Main;
import com.github.phalexei.dismake.Target;
import com.github.phalexei.dismake.parser.Parser;
import com.github.phalexei.dismake.parser.Parser.DependencyNotFoundException;
import com.github.phalexei.dismake.work.Result;
import com.github.phalexei.dismake.work.Task;

import java.io.FileOutputStream;
import java.io.IOException;
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

    private final Queue<Task> tasks;
    private final Map<String, Target> lockedTasks;
    private final Object hangingClients;

    public RmiServerImpl(String url, String fileName, String theTarget) throws IOException, DependencyNotFoundException, MainTargetNotFoundException {
        super(0);    // required to avoid the 'rmic' step
        System.out.println("RMI server started on " + url);

        try { //special exception handler for registry creation
            LocateRegistry.createRegistry(1099);
            System.out.println("java RMI registry created.");
        } catch (RemoteException e) {
            //do nothing, error means registry already exists
            System.out.println("java RMI registry already exists.");
        }

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
            for (Target t : map.values()) {
                if (mainTarget.dependsOn(t.getName())) {
                    if (!t.available()) {
                        lockedTasks.put(t.getName(), t);
                    } else {
                        tasks.add(new Task(t));
                    }
                }
            }
            hangingClients = new Object();
        } else {
            throw new MainTargetNotFoundException("Target : " + theTarget +
                    "not found in " + fileName);
        }

        // Bind this object instance to the name "RmiServer"
        Naming.rebind("//" + url + "/RmiServer", this);
        System.out.println("PeerServer bound in registry");
    }

    @Override
    public Task getTask() throws RemoteException {
        if (tasks.size() > 0) {
            return tasks.poll();
        } else if (lockedTasks.size() > 0) { // no task available right now, but in the future there will be
            synchronized (hangingClients) {
                try {
                    hangingClients.wait();
                } catch (InterruptedException e) {
                    //TODO exception handling
                    e.printStackTrace();
                }
            }
            return tasks.size() > 0 ? tasks.poll() : null;
        } else { // nothing more to do
            return null;
        }
    }

    @Override
    public void sendResults(Result result) throws RemoteException {
        if (result.getExitCode() == 0) { // exit code for "success"
            onTaskSuccess(result.getTaskName(), result.getFile());
        } else { // failure during build
            onTaskFailure(result.getTaskName(), result.getExitCode(), result.getStdErr());
        }
    }

    private void onTaskFailure(String taskName, int exitCode, String stdErr) {
        //TODO: stop the whole process (maybe ?) and display error properly
        System.out.println(taskName + " failed with error code: " + exitCode);
        System.out.println("More information:");
        System.out.println(stdErr);
        System.exit(exitCode);
    }

    private void onTaskSuccess(String taskName, byte[] file) {
        synchronized (hangingClients) {
            for (Target t : lockedTasks.values()) {
                if (t.getDependencies().containsKey(taskName)) {
                    t.resolveOneDependency();
                    if (t.available()) {
                        try {
                            tasks.add(new Task(t));
                            lockedTasks.remove(t.getName());
                            hangingClients.notify();
                        } catch (IOException e) {
                            //TODO exception handling
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (tasks.size() == 0 && lockedTasks.size() == 0) { // no more tasks, wake every hanging process
                hangingClients.notifyAll();
                System.out.println("DisMake terminated successfully :-)");
                System.out.println("Server shutting down.");
                System.exit(0);
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(taskName);
            fos.write(file);
            fos.close();
        } catch (IOException e) {
            //TODO exception handling
            e.printStackTrace();
        }
    }
}
