package com.hungerbridge.common;

import com.hungerbridge.common.endpoints.Health;
import com.hungerbridge.common.endpoints.Log;
import com.hungerbridge.common.endpoints.RootHandler;
import com.hungerbridge.common.endpoints.Run;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {

    private final Config config;
    private HttpServer server;

    public Server(Config config) {
        this.config = config;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", config.port), 0);

        RootHandler root = new RootHandler(config);
        Run run = new Run();
        Log log = new Log();
        Health health = new Health();

        server.createContext("/", root);
        server.createContext("/run", root.wrap(run));
        server.createContext("/log", root.wrap(log));
        server.createContext("/health", root.wrap(health));

        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}
