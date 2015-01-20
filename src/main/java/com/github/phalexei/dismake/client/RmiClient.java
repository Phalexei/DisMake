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
            System.out.println(Main.PREFIX + "Client exiting.");
        }
    }

    private Result work(Task myTask) throws InterruptedException, RemoteException {
        Result result = null;

        System.out.println(Main.PREFIX + "Copying dependencies from Server");
        for (Map.Entry<String, byte[]> file : myTask.getFiles().entrySet()) {
            try {
                System.out.println(Main.PREFIX + "\tCopying " + file.getKey());
                Files.write(Paths.get(file.getKey()), file.getValue());
            } catch (IOException e) {
                this.server.errorOnTask(myTask);
            }
        }
        System.out.println(Main.PREFIX + "Copy complete");

        try {
            String[] cmd = new String[]{
                    "/bin/sh",
                    "-c",
                    "PATH=.:$PATH " + myTask.getTarget().getCommand()
            };
            System.out.println(Main.PREFIX + "Executing " + Arrays.toString(cmd));
            Process p = Runtime.getRuntime().exec(cmd);
            System.out.println(Main.PREFIX + "Done.");

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
