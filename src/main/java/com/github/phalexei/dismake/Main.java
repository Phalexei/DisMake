package com.github.phalexei.dismake;

import com.github.phalexei.dismake.client.RmiClient;
import com.github.phalexei.dismake.parser.Parser.DependencyNotFoundException;
import com.github.phalexei.dismake.server.RmiServerImpl;
import com.github.phalexei.dismake.server.RmiServerImpl.MainTargetNotFoundException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;

/**
 * Program entry point.
 */
public class Main {

    public static final String FIRST = "\\#~}]@|`\\^@]First"; //lol

    /**
     * Program entry point.
     *
     * @param args arguments
     * @throws Exception if anything goes wrong
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            error();
            return;
        }

        String firstArg = args[0].toLowerCase();
        if ("--server".equals(firstArg)) {
            startServer(Arrays.copyOfRange(args, 1, args.length));
        } else if ("--client".equals(firstArg)) {
            startClient(Arrays.copyOfRange(args, 1, args.length));
        } else {
            error();
            return;
        }
    }

    /**
     * Entry point for Server mode.
     *
     * @param args arguments
     * @throws IOException if anything goes wrong
     */
    private static void startServer(String[] args) throws IOException {
        if (args.length != 2 && args.length != 3) {
            error();
            return;
        }

        String serverUrl = args[0];
        String makefile = args[1];
        String target = args.length == 3 ? args[2] : null;

        try {
            new RmiServerImpl(serverUrl, makefile, target);
        } catch (DependencyNotFoundException | MainTargetNotFoundException e) {
            // TODO Handle Exception
            e.printStackTrace();
        }
    }

    /**
     * Entry point for Client mode.
     *
     * @param args arguments
     * @throws RemoteException if anything goes wrong
     * @throws NotBoundException if anything else goes wrong
     * @throws MalformedURLException if something else goes wrong
     */
    private static void startClient(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        if (args.length != 2) {
            error();
            return;
        }

        String serverUrl = args[0];
        int nbThread = Integer.parseInt(args[1]);
        try {
            //int cores = Runtime.getRuntime().availableProcessors();
            Thread[] threads = new Thread[nbThread];
            for (int i = 0; i < nbThread; i++) {
                threads[i] =  new Thread(new RmiClient(serverUrl));
                threads[i].start();
            }
        } catch (ConnectException | ConnectIOException e) {
            System.out.println("Failed to connect to Server!");
            System.exit(1337);
        }
    }

    /**
     * Called when the program is misused.
     */
    private static void error() {
        System.out.println("Usage: <cmd> --server <serverUrl> <pathToMakefile>");
        System.out.println("       <cmd> --client <serverUrl>");
        System.exit(42);
    }
}
