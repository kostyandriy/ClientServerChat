package edu.school21.sockets.app;

import edu.school21.sockets.client.Client;

public class Main {

    private static int port;

    public static void main(String[] args) {
        parseArgs(args);

        Client client = new Client();
        client.start(port);
    }

    private static void parseArgs(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected exactly one argument: --server-port=<port>");
        }

        String arg = args[0];
        if (!arg.startsWith("--server-port=")) {
            throw new IllegalArgumentException("Expected argument format: --server-port=<port>");
        }

        try {
            port = Integer.parseInt(arg.substring("--server-port=".length()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Port must be an integer.");
        }

        if (port < 1024 || port > 49151) {
            throw new IllegalArgumentException("Port must be between 1024 and 49151.");
        }
    }
}