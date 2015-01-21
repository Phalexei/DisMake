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
import java.util.Map.Entry;
import java.util.Set;

/**
 * //TODO doc
 */
public class RmiClient implements Runnable {

    private final RmiServer server;
    private final boolean   debugMode;

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

        Set<Entry<String, byte[]>> dependencies = myTask.getFiles().entrySet();
        if (dependencies.size() > 0) {
            if (this.debugMode) {
                Main.print("Copying " + dependencies.size() + " dependencies from Server");
            }
            long lastPrintTime = 0;
            int fileCount = 1;
            for (Entry<String, byte[]> file : dependencies) {
                if (file.getValue() != null) {
                    try {
                        if (this.debugMode && System.currentTimeMillis() - 100 > lastPrintTime) {
                            lastPrintTime = System.currentTimeMillis();
                            Main.print("\tCopying file " + fileCount + "/" + dependencies.size() + "...");
                        }
                        Files.write(Paths.get(file.getKey()), file.getValue());
                        fileCount++;
                    } catch (IOException e) {
                        this.server.errorOnTask(myTask);
                    }
                }
            }
            if (this.debugMode) {
                Main.print("Copy complete");
            } else {
                Main.print("Copied " + dependencies.size() + "dependencies from Server");
            }
        }

        try {
            String[] cmd = new String[]{
                    "/bin/sh",
                    "-c",
                    "PATH=.:$PATH " + myTask.getTarget().getCommand()
            };
            Main.print("Executing " + Arrays.toString(cmd));
            Process p = Runtime.getRuntime().exec(cmd);

            String stdOut = IOUtils.toString(p.getInputStream());
            String stdErr = IOUtils.toString(p.getErrorStream());

            p.waitFor();
            Main.print("Done.");
            result = new Result(myTask, stdOut, stdErr, p.exitValue());
        } catch (IOException e) {
            //TODO exception handling
            e.printStackTrace();
        }

        return result;
    }
}
