package com.github.phalexei.dismake;

import com.github.phalexei.dismake.client.RmiClient;
import com.github.phalexei.dismake.parser.Parser.DependencyNotFoundException;
import com.github.phalexei.dismake.server.RmiServerImpl;
import com.github.phalexei.dismake.server.RmiServerImpl.MainTargetNotFoundException;

public class Main {

    public static final String FIRST = "\\#~}]@|`\\^@]First"; //lol

    public static void main(String[] args) throws Exception {
        Boolean isServer = null;
        String serverUrl = null;
        String makeFile = null;
        String target = null;
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
                    if (args.length > i + 1) {
                        target = args[++i];
                    }
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

        if (isServer) {
            try {
                new RmiServerImpl(serverUrl, makeFile, target);
            } catch (DependencyNotFoundException | MainTargetNotFoundException e) {
                //TODO exception handling
                e.printStackTrace();
            }
        } else {
            RmiClient client = new RmiClient(serverUrl);
            client.mainLoop();
        }
    }

    private static void error() {
        //TODO : this function lol
        System.out.println("NOPE");
        System.exit(42);
    }
}
