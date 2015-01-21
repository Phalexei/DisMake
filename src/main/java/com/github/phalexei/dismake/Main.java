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
import java.util.HashMap;
import java.util.Map;

/**
 * Program entry point.
 */
public class Main {

    /**
     * Internal key used to save the main target.
     */
    public static final String FIRST = " :: ";

    public static String PRINT_PREFIX;

    private static final Map<String, Integer> printId = new HashMap<>();

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
        }
    }

    /**
     * Entry point for Server mode.
     *
     * @param args arguments
     * @throws IOException if anything goes wrong
     */
    private static void startServer(String[] args) throws IOException {
        PRINT_PREFIX = "[Server " + InetAddress.getLocalHost().getCanonicalHostName() + "  ] ";

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
        PRINT_PREFIX = "[Client " + InetAddress.getLocalHost().getCanonicalHostName() + ":__id] ";

        if (args.length != 2 && args.length != 3) {
            error();
            return;
        }

        int nbCores = Runtime.getRuntime().availableProcessors();

        String serverUrl = args[0];
        float threadRatio = Integer.parseInt(args[1]) / 100f;
        int nbThreads = (int) Math.max(1, Math.floor(nbCores * threadRatio));
        boolean debugMode;
        if (args.length == 3) {
            if ("debug".equalsIgnoreCase(args[2])) {
                debugMode = true;
            } else {
                error();
                return;
            }
        } else {
            debugMode = false;
        }
        try {
            Thread[] threads = new Thread[nbThreads];
            for (int i = 0; i < nbThreads; i++) {
                threads[i] = new Thread(new RmiClient(serverUrl, debugMode));
                printId.put(threads[i].getName(), i);
                threads[i].start();
            }
            for (int i = 0; i < nbThreads; i++) {
                threads[i].join();
            }
        } catch (ConnectException | ConnectIOException e) {
            Main.print("Failed to connect to Server!");
            System.exit(1337);
        }
    }

    public static void print(String string) {
        String threadName = Thread.currentThread().getName();
        Integer id = printId.get(threadName);
        System.out.println(PRINT_PREFIX.replace("__id", id == null ? "x" : id.toString()) + string);
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
