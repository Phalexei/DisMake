package com.github.phalexei.dismake.server;

import com.github.phalexei.dismake.Target;
import com.github.phalexei.dismake.parser.Parser;
import com.github.phalexei.dismake.parser.Parser.DependencyNotFoundException;
import com.github.phalexei.dismake.work.Task;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * RmiServer
 *
 * Basic implentation of a server
 */
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

        lockedTasks = new HashMap<>();
        tasks = new ConcurrentLinkedQueue<>();
        for (Target t : map.values()) {
            if (t.getDependencies().size() > 0) {
                lockedTasks.put(t.getName(), t);
            } else {
                tasks.add(new Task(t));
            }
        }
    }

    @Override
    public Task getTask() throws RemoteException {
        return tasks.poll();
    }

    @Override
    public void sendResults(Task doneTask) throws RemoteException {
        System.out.println("task done : " + doneTask.getTarget().getName());
        final String taskName = doneTask.getTarget().getName();
        for (Target t : lockedTasks.values()) {
            if (t.getDependencies().containsKey(taskName)) {
                t.resolveOneDependency();
                if (t.available()) {
                    tasks.add(new Task(t));
                    System.out.println("task available : " + t.getName());
                }
            }
        }
    }
}
