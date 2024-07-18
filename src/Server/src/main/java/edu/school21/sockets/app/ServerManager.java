package edu.school21.sockets.app;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import edu.school21.sockets.config.SocketsApplicationConfig;
import edu.school21.sockets.server.Server;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Parameters(separators = "=")
public class ServerManager {
    @Parameter(names = {"--port"}, description = "Number of port",
            required = true)
    private static int port;

    public void start(String[] args) {
        parseArgs(args);

        ApplicationContext context = new AnnotationConfigApplicationContext(SocketsApplicationConfig.class);
        Server server = context.getBean("Server", Server.class);

        server.runServer(port);
    }

    private void parseArgs(String[] args) {
        try {
            JCommander.newBuilder().addObject(this).build().parse(args);
            testPort();
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    private static void testPort() {
        if (port < 1024 || port > 49151) {
            throw new RuntimeException("port must be 1024 <= port <= 49151");
        }
    }
}
