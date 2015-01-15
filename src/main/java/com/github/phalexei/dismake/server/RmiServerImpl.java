package com.github.phalexei.dismake.server;

import com.github.phalexei.dismake.Target;
import com.github.phalexei.dismake.parser.Parser;
import com.github.phalexei.dismake.parser.Parser.DependencyNotFoundException;
import com.github.phalexei.dismake.work.Result;
import com.github.phalexei.dismake.work.Task;
import com.github.phalexei.dismake.work.TaskType;

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
    private final Queue<Task> tasks;
    private final Map<String, Target> lockedTasks;

    public RmiServerImpl(String url, String fileName) throws IOException, DependencyNotFoundException {
        super(0);    // required to avoid the 'rmic' step
        System.out.println("RMI server started on " + url);

        try { //special exception handler for registry creation
            LocateRegistry.createRegistry(1099);
            System.out.println("java RMI registry created.");
        } catch (RemoteException e) {
            //do nothing, error means registry already exists
            System.out.println("java RMI registry already exists.");
        }

        // Bind this object instance to the name "RmiServer"
        Naming.rebind("//" + url + "/RmiServer", this);
        System.out.println("PeerServer bound in registry");

        Map<String, Target> map = Parser.parse(fileName);

        lockedTasks = new ConcurrentHashMap<>();
        tasks = new ConcurrentLinkedQueue<>();
        for (Target t : map.values()) {
            if (!t.available()) {
                lockedTasks.put(t.getName(), t);
            } else {
                tasks.add(new Task(t));
            }
        }
    }

    @Override
    public Task getTask() throws RemoteException {
        if (tasks.size() > 0) {
            return tasks.poll();
        } else if (lockedTasks.size() > 0) {
            return new Task(TaskType.WAIT);
        } else {
            return new Task(TaskType.NO_MORE_WORK);
        }
    }

    @Override
    public void sendResults(Result result) throws RemoteException {
        System.out.println("task done : " + result.getTaskName());
        final String taskName = result.getTaskName();
        for (Target t : lockedTasks.values()) {
            if (t.getDependencies().containsKey(taskName)) {
                t.resolveOneDependency();
                if (t.available()) {
                    try {
                        tasks.add(new Task(t));
                        lockedTasks.remove(t.getName());
                    } catch (IOException e) {
                        //TODO
                        e.printStackTrace();
                    }
                    System.out.println("task available : " + t.getName());
                }
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(taskName);
            fos.write(result.getFile());
            fos.close();
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
        }
    }
}
