package com.github.phalexei.dismake.client;

import com.github.phalexei.dismake.server.RmiServer;

import java.rmi.Naming;

/*
 * Simple client asking server for 10 messages
 */
public class RmiClient {
    public static void main(String args[]) throws Exception {
        RmiServer obj = (RmiServer)Naming.lookup("//localhost/RmiServer");
        for (int i = 0; i < 10; i++)
            System.out.println(obj.getMessage());
    }
}
