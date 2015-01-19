package com.github.phalexei.dismake.client;

import com.github.phalexei.dismake.server.RmiServer;
import com.github.phalexei.dismake.work.Result;
import com.github.phalexei.dismake.work.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;

/**
 * //TODO doc
 */
public class RmiClient {
    private final RmiServer server;
    public RmiClient(String serverUrl) throws RemoteException, NotBoundException, MalformedURLException {
        this.server = (RmiServer)Naming.lookup("//"+serverUrl+"/RmiServer");
    }

    public void mainLoop() throws RemoteException {
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

    private Result work(Task myTask) throws InterruptedException {
        Result result = null;

        //TODO: write files to disk ?
        for (byte[] file : myTask.getFiles()) {
            System.out.println("file : ");
            System.out.println(file);
        }
        try {
            String[] cmd = new String[]{
                    "/bin/sh",
                    "-c",
                    "PATH=.:$PATH " + myTask.getTarget().getCommand()
            };
            System.out.println("Executing " + Arrays.toString(cmd) + "on " + InetAddress.getLocalHost().getCanonicalHostName());
            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            String s;
            // read the output from the command
            String stdOut = "";
            while ((s = stdInput.readLine()) != null) {
                stdOut += s;
            }

            // read any errors from the attempted command
            String stdErr = "";
            while ((s = stdError.readLine()) != null) {
                stdErr += s;
            }

            p.waitFor();
            result = new Result(myTask, stdOut, stdErr, p.exitValue());
        } catch (IOException e) {
            //TODO exception handling
            e.printStackTrace();
        }

        return result;
    }
}
