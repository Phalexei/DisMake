package com.github.phalexei.dismake;

import com.github.phalexei.dismake.client.RmiClient;
import com.github.phalexei.dismake.server.RmiServer;

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
        if (isServer == null || !isServer && serverUrl == null) {
            error();
            return;
        }

        // TODO Start Server or Client
        if (isServer) {
            RmiServer.main(new String[]{makeFile});
        } else {
            RmiClient.main(new String[]{serverUrl});
        }
    }

    private static void error() {
        System.out.println("NOPE");
        System.exit(42);
    }
}
