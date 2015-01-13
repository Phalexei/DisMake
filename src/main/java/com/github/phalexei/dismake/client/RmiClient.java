package com.github.phalexei.dismake.client;

import com.github.phalexei.dismake.server.RmiServer;
import com.github.phalexei.dismake.work.Task;

import java.rmi.Naming;

/*
 * Simple client asking server for 10 messages
 */
public class RmiClient {
    public static void main(String args[]) throws Exception {
        RmiServer obj = (RmiServer)Naming.lookup("//localhost/RmiServer");

        Task myTask;
        while (true) { //TODO: end condition
            myTask = obj.getTask();

            if (myTask != null) {
                work(myTask);
                obj.sendResults(myTask);
            } else { // no work for now
                System.out.println("no work, idling");
                Thread.sleep(5000);
            }
        }
    }

    private static void work(Task myTask) {
        //TODO
    }
}
