package com.github.phalexei.dismake.client;

import com.github.phalexei.dismake.server.RmiServer;
import com.github.phalexei.dismake.work.Result;
import com.github.phalexei.dismake.work.Task;
import com.github.phalexei.dismake.work.TaskType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;

public class RmiClient {
    private final RmiServer server;
    private boolean work;
    public RmiClient(String serverUrl) throws RemoteException, NotBoundException, MalformedURLException {
        this.server = (RmiServer)Naming.lookup("//"+serverUrl+"/RmiServer");
        this.work = true;
    }

    public void mainLoop() throws RemoteException {
        Task myTask;
        while (work) {
            myTask = server.getTask();

            if (myTask.getType() == TaskType.NO_MORE_WORK) {
                work = false;
            } else if (myTask.getType() == TaskType.WAIT) {
                //TODO
            } else {
                server.sendResults(work(myTask));
            }
        }
    }

    private Result work(Task myTask) {
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
                    myTask.getTarget().getCommand()
            };
            System.out.println("Executing " + Arrays.toString(cmd));
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

            result = new Result(myTask, stdOut, stdErr, p.exitValue());
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
        }

        return result;
    }
}
