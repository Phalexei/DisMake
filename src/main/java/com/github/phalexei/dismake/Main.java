package com.github.phalexei.dismake;

import com.github.phalexei.dismake.client.RmiClient;
import com.github.phalexei.dismake.parser.Parser.DependencyNotFoundException;
import com.github.phalexei.dismake.server.RmiServer;
import com.github.phalexei.dismake.server.RmiServerImpl;

public class Main {

    public static void main(String[] args) throws Exception {
        Boolean isServer = null;
        String serverUrl = null;
        String makeFile = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "--server":
                    if (isServer != null || i == args.length - 1) {
                        error();
                        return;
                    }
                    isServer = true;
                    serverUrl = args[++i];
                    makeFile = args[++i];
                    break;
                case "--client":
                    if (isServer != null || i == args.length - 1) {
                        error();
                        return;
                    }
                    isServer = false;
                    serverUrl = args[++i];
                    break;
                default:
                    error();
                    break;
            }
        }
        if (isServer == null || serverUrl == null) {
            error();
            return;
        }

        // TODO Start Server or Client
        if (isServer) {
            try {
                RmiServer server = new RmiServerImpl(serverUrl, makeFile);
            } catch (DependencyNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            RmiClient client = new RmiClient(serverUrl);
            client.mainLoop();
        }
    }

    private static void error() {
        System.out.println("NOPE");
        System.exit(42);
    }
}
