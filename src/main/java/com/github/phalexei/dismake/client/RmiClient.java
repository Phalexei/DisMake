package com.github.phalexei.dismake.client;

import com.github.phalexei.dismake.Main;
import com.github.phalexei.dismake.server.RmiServer;
import com.github.phalexei.dismake.work.Result;
import com.github.phalexei.dismake.work.Task;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Map;

/**
 * //TODO doc
 */
public class RmiClient implements Runnable {

    private final RmiServer server;
    private final boolean debugMode;

    public RmiClient(String serverUrl, boolean debugMode) throws RemoteException, NotBoundException, MalformedURLException {
        this.server = (RmiServer) Naming.lookup("//" + serverUrl + "/RmiServer");
        this.debugMode = debugMode;
    }

    public void run() {
        try {
            Task myTask;
            boolean work = true;
            while (work) {
                myTask = server.getTask();

                if (myTask != null) {
                    try {
                        server.sendResults(work(myTask));
                    } catch (InterruptedException e) {
                        work = false;
                    }
                } else {
                    work = false;
                }
            }
        } catch (RemoteException e) {
            // Something wrong happened, but maybe it's normal.
            if (this.debugMode) {
                e.printStackTrace();
            }
        } finally {
            Main.print("Client exiting.");
        }
    }

    private Result work(Task myTask) throws InterruptedException, RemoteException {
        Result result = null;

        Main.print("Copying dependencies from Server");
        for (Map.Entry<String, byte[]> file : myTask.getFiles().entrySet()) {
            try {
                Main.print("\tCopying " + file.getKey());
                Files.write(Paths.get(file.getKey()), file.getValue());
            } catch (IOException e) {
                this.server.errorOnTask(myTask);
            }
        }
        Main.print("Copy complete");

        try {
            if (myTask.getTarget().getCommand() == null) {
                Main.print("Target '" + myTask.getTarget().getName() + "' has no associated command, skipping.");
                result = new Result(myTask, "", "", 0);
            } else {
                String[] cmd = new String[]{
                        "/bin/sh",
                        "-c",
                        "PATH=.:$PATH " + myTask.getTarget().getCommand()
                };
                Main.print("Executing " + Arrays.toString(cmd));
                Process p = Runtime.getRuntime().exec(cmd);
                Main.print("Done.");

                String stdOut = IOUtils.toString(p.getInputStream());
                String stdErr = IOUtils.toString(p.getErrorStream());

                p.waitFor();
                result = new Result(myTask, stdOut, stdErr, p.exitValue());
            }
        } catch (IOException e) {
            //TODO exception handling
            e.printStackTrace();
        }

        return result;
    }
}
