package com.github.phalexei.dismake.client;

import com.github.phalexei.dismake.server.RmiServer;
import com.github.phalexei.dismake.work.Result;
import com.github.phalexei.dismake.work.Task;
import com.github.phalexei.dismake.work.TaskType;

import java.io.IOException;
import java.net.MalformedURLException;
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

        //TODO run the command and gather output files

        try {
            result = new Result(myTask);
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
        }

        return result;
    }
}
