package com.github.phalexei.dismake.client;

import com.github.phalexei.dismake.server.RmiServer;
import com.github.phalexei.dismake.work.Result;
import com.github.phalexei.dismake.work.Task;
import com.github.phalexei.dismake.work.TaskType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

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

        for (byte[] file : myTask.getFiles()) {
            System.out.println("file : ");
            System.out.println(file);
        }
        //TODO run the command and gather output files
        try {
            String s = null;
            Process p = Runtime.getRuntime().exec(myTask.getTarget().getCommand());

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

            result = new Result(myTask, Paths.get(myTask.getTarget().getName()));
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
        }

        return result;
    }
}
