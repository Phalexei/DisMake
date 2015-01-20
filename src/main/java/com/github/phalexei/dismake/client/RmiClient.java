package com.github.phalexei.dismake.client;

import com.github.phalexei.dismake.server.RmiServer;
import com.github.phalexei.dismake.work.Result;
import com.github.phalexei.dismake.work.Task;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
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

    public RmiClient(String serverUrl) throws RemoteException, NotBoundException, MalformedURLException {
        this.server = (RmiServer) Naming.lookup("//" + serverUrl + "/RmiServer");
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
        } catch (RemoteException ignored) {
            // somethin happened to the server, client should die silently
        } finally {
            System.out.println("Client exiting.");
        }
    }

    private Result work(Task myTask) throws InterruptedException, RemoteException {
        Result result = null;

        for (Map.Entry<String, BufferedReader> file : myTask.getFiles().entrySet()) {
            try {
                IOUtils.copy(file.getValue(), Files.newBufferedWriter(Paths.get(file.getKey()), StandardCharsets.UTF_8));
            } catch (IOException e) {
                this.server.errorOnTask(myTask);
            }
        }

        try {
            String[] cmd = new String[]{
                    "/bin/sh",
                    "-c",
                    "PATH=.:$PATH " + myTask.getTarget().getCommand()
            };
            System.out.println("Executing " + Arrays.toString(cmd) + " on " + InetAddress.getLocalHost().getCanonicalHostName());
            Process p = Runtime.getRuntime().exec(cmd);

            String stdOut = IOUtils.toString(p.getInputStream());
            String stdErr = IOUtils.toString(p.getErrorStream());

            p.waitFor();
            result = new Result(myTask, stdOut, stdErr, p.exitValue());
        } catch (IOException e) {
            //TODO exception handling
            e.printStackTrace();
        }

        return result;
    }
}
