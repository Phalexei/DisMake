package com.github.phalexei.dismake;

import com.github.phalexei.dismake.client.RmiClient;
import com.github.phalexei.dismake.parser.Parser.DependencyNotFoundException;
import com.github.phalexei.dismake.server.RmiServerImpl;
import com.github.phalexei.dismake.server.RmiServerImpl.MainTargetNotFoundException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;

/**
 * Program entry point.
 */
public class Main {

    /**
     * Internal key used to save the main target.
     */
    public static final String FIRST = " :: ";

    public static String PREFIX;

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
        PREFIX = "[Server " + InetAddress.getLocalHost().getCanonicalHostName().replaceAll("[a-zA-Z\\.]+", "") + "] ";
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
    private static void startClient(String[] args) throws RemoteException, NotBoundException, MalformedURLException, InterruptedException, UnknownHostException {
        PREFIX = "[Client " + InetAddress.getLocalHost().getCanonicalHostName().replaceAll("[a-zA-Z\\.]+", "") + "] ";
        if (args.length != 2) {
            error();
            return;
        }

        int nbCores = Runtime.getRuntime().availableProcessors();

        String serverUrl = args[0];
        float threadRatio = Integer.parseInt(args[1]) / 100f;
        int nbThreads = (int) Math.min(1, Math.floor(nbCores * threadRatio));
        try {
            Thread[] threads = new Thread[nbThreads];
            for (int i = 0; i < nbThreads; i++) {
                threads[i] = new Thread(new RmiClient(serverUrl));
                threads[i].start();
            }
            for (int i = 0; i < nbThreads; i++) {
                threads[i].join();
            }
        } catch (ConnectException | ConnectIOException e) {
            System.out.println(PREFIX + "Failed to connect to Server!");
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
