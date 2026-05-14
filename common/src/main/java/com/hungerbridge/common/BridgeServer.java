package com.hungerbridge.common;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Minimal non-blocking HTTP server that exposes /run and /log endpoints.
 * Platform-specific modules provide logging and command execution.
 */
public final class BridgeServer {

    private final Config config;
    private final Logger logger;
    private final CommandExecutor commandExecutor;

    private HttpServer server;
    private ExecutorService executor;

    public BridgeServer(Config config, Logger logger, CommandExecutor commandExecutor) {
        this.config = config;
        this.logger = logger;
        this.commandExecutor = commandExecutor;
    }

    /**
     * Start the HTTP server on the configured port.
     */
    public synchronized void start() {
        if (server != null) {
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
        } catch (IOException e) {
            throw new RuntimeException("Failed to bind HTTP server on port " + config.getPort(), e);
        }

        executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);

        server.createContext("/run", new RunHandler());
        server.createContext("/log", new LogHandler());

        server.start();
        logger.log("INFO", "HungerBridge HTTP server started on port " + config.getPort());
    }

    /**
     * Stop the HTTP server and release resources.
     */
    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        logger.log("INFO", "HungerBridge HTTP server stopped.");
    }

    private boolean isAuthorized(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("X-Auth-Key");
        return header != null && header.equals(config.getAuthKey());
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            byte[] bytes = in.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8).trim();
        }
    }

    private void writeResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private class RunHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                writeResponse(exchange, 405, "method not allowed");
                return;
            }

            if (!config.isEnabledRun()) {
                writeResponse(exchange, 403, "run disabled");
                return;
            }

            if (!isAuthorized(exchange)) {
                writeResponse(exchange, 401, "unauthorized");
                return;
            }

            String command = readBody(exchange);
            if (command.isEmpty()) {
                writeResponse(exchange, 400, "empty command");
                return;
            }

            logger.log("INFO", "Executing command via /run: " + command);
            // No output capture: fire-and-forget
            commandExecutor.execute(command);
            writeResponse(exchange, 200, "ok");
        }
    }

    private class LogHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                writeResponse(exchange, 405, "method not allowed");
                return;
            }

            if (!config.isEnabledLog()) {
                writeResponse(exchange, 403, "log disabled");
                return;
            }

            if (!isAuthorized(exchange)) {
                writeResponse(exchange, 401, "unauthorized");
                return;
            }

            String message = readBody(exchange);
            if (message.isEmpty()) {
                writeResponse(exchange, 400, "empty message");
                return;
            }

            logger.log("INFO", "[HungerBridge] " + message);
            writeResponse(exchange, 200, "ok");
        }
    }
}
