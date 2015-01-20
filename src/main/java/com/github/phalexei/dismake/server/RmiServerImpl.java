package com.github.phalexei.dismake.server;

import com.github.phalexei.dismake.Main;
import com.github.phalexei.dismake.parser.Parser;
import com.github.phalexei.dismake.parser.Parser.DependencyNotFoundException;
import com.github.phalexei.dismake.work.Result;
import com.github.phalexei.dismake.work.Target;
import com.github.phalexei.dismake.work.Task;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
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

    private final String              url;
    private final Queue<Task>         tasks;
    private final Map<String, Target> lockedTasks;
    private final Object              hangingClients;
    private       boolean             parsingDone;

    public RmiServerImpl(String url, String fileName, String theTarget) throws IOException, DependencyNotFoundException, MainTargetNotFoundException {
        super(0);    // required to avoid the 'rmic' step
        this.parsingDone = false;
        this.url = url;
        System.out.println(Main.PREFIX + "RMI server started on " + this.url);

        try { //special exception handler for registry creation
            LocateRegistry.createRegistry(1099);
            System.out.println(Main.PREFIX + "java RMI registry created.");
        } catch (RemoteException e) {
            //do nothing, error means registry already exists
            System.out.println(Main.PREFIX + "java RMI registry already exists.");
        }

        hangingClients = new Object();
        // Bind this object instance to the name "RmiServer"
        Naming.rebind("//" + this.url + "/RmiServer", this);
        System.out.println(Main.PREFIX + "PeerServer bound in registry");

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
            throw new MainTargetNotFoundException("Target : " + theTarget +
                                                          "not found in " + fileName);
        }
        this.parsingDone = true;
        synchronized (this.hangingClients) {
            this.hangingClients.notifyAll();
        }
    }

    @Override
    public Task getTask() throws RemoteException {
        synchronized (this.hangingClients) {
            while (!this.parsingDone) {
                try {
                    hangingClients.wait();
                } catch (InterruptedException e) {
                    //TODO exception handling
                    e.printStackTrace();
                }
            }
            if (tasks.size() > 0) {
                return tasks.poll();
            } else if (lockedTasks.size() > 0) { // no task available right now, but in the future there will be
                try {
                    hangingClients.wait();
                } catch (InterruptedException e) {
                    //TODO exception handling
                    e.printStackTrace();
                }

                return tasks.size() > 0 ? tasks.poll() : null;
            } else { // nothing more to do
                return null;
            }
        }
    }

    @Override
    public void sendResults(Result result) {
        if (result.getExitCode() == 0) { // exit code for "success"
            onTaskSuccess(result.getFileName(), result.getFileReader());
        } else { // failure during build
            onTaskFailure(result.getFileName(), result.getExitCode(), result.getStdErr());
        }
    }

    @Override
    public void errorOnTask(Task failedTask) throws RemoteException {
        tasks.add(failedTask);
    }

    private void onTaskFailure(String taskName, int exitCode, String stdErr) {
        //TODO: stop the whole process (maybe ?) and display error properly
        System.out.println(Main.PREFIX + taskName + " failed with error code: " + exitCode);
        System.out.println(Main.PREFIX + "More information:");
        System.out.println(stdErr);
        System.exit(exitCode);
    }

    private void onTaskSuccess(String fileName, BufferedReader fileReader) {
        System.out.println(Main.PREFIX + "Copying result file " + fileName + " from client");
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), StandardCharsets.UTF_8)) {
            IOUtils.copy(fileReader, writer);
        } catch (IOException e) {
            //TODO exception handling
            e.printStackTrace();
        }

        synchronized (hangingClients) {
            for (Target t : lockedTasks.values()) {
                if (t.getDependencies().containsKey(fileName)) {
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
                System.out.println(Main.PREFIX+"DisMake terminated successfully :-)");
                System.out.println(Main.PREFIX+"Server shutting down.");
                try {
                    Naming.unbind("//" + this.url + "/RmiServer");
                } catch (RemoteException | MalformedURLException | NotBoundException e) {
                    System.exit(0);
                }
            }
        }
    }
}
