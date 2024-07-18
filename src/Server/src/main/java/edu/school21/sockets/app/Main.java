package edu.school21.sockets.app;

import com.beust.jcommander.Parameters;

@Parameters(separators = "=")
public class Main {

    public static void main(String[] args) {
        ServerManager manager = new ServerManager();
        manager.start(args);
    }
}