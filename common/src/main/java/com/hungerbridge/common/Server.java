package com.hungerbridge.common;

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

        RootHandler handler = new RootHandler(config);

        server.createContext("/", handler); // catch-all router

        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}
