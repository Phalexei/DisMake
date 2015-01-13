package com.github.phalexei.dismake.server;

import com.github.phalexei.dismake.parser.Parser;
import com.github.phalexei.dismake.parser.Parser.DependencyNotFoundException;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

/*
 * RmiServer
 *
 * Basic implentation of a server
 */
public class RmiServerImpl extends UnicastRemoteObject implements RmiServer {
    public static int i = 0;

    public RmiServerImpl() throws RemoteException {
        super(0);    // required to avoid the 'rmic' step
    }

    @Override
    public String getMessage() {
        return String.valueOf(i++);
    }

    public static void main(String args[]) throws Exception {
        System.out.println("RMI server started");

        try { //special exception handler for registry creation
            LocateRegistry.createRegistry(1099);
            System.out.println("java RMI registry created.");
        } catch (RemoteException e) {
            //do nothing, error means registry already exists
            System.out.println("java RMI registry already exists.");
        }

        //Instantiate RmiServer
        RmiServerImpl obj = new RmiServerImpl();

        // Bind this object instance to the name "RmiServer"
        Naming.rebind("//localhost/RmiServer", obj);
        System.out.println("PeerServer bound in registry");

        if (args.length > 0) {
            try {
                //TODO: use return value
                Parser.parse(args[0]);
            } catch (DependencyNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
