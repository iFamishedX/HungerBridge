package com.hungerbridge.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public synchronized void start() {
        if (server != null) return;

        try {
            server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
        } catch (IOException e) {
            throw new RuntimeException("Failed to bind HTTP server on port " + config.getPort(), e);
        }

        executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);

        // Legacy endpoints
        server.createContext("/run", new LegacyRunHandler());
        server.createContext("/log", new LegacyLogHandler());

        // JSON v1 endpoints
        server.createContext("/v1/run", new RunV1Handler());
        server.createContext("/v1/log", new LogV1Handler());
        server.createContext("/v1/status", new StatusV1Handler());
        server.createContext("/v1/version", new VersionV1Handler());

        server.start();
        logger.log("INFO", "HungerBridge HTTP server started on port " + config.getPort());
    }

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

    // -------------------------
    // Helpers
    // -------------------------

    private boolean isAuthorized(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("X-Auth-Key");
        return header != null && header.equals(config.getAuthKey());
    }

    private JsonObject readJson(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            String body = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
            if (body.isEmpty()) return null;
            return JsonParser.parseString(body).getAsJsonObject();
        }
    }

    private void writeJson(HttpExchange exchange, int status, JsonObject body) throws IOException {
        byte[] bytes = Json.stringify(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private void writeError(HttpExchange exchange, int status, String error, String message) throws IOException {
        writeJson(exchange, status, Json.obj(
                "ok", false,
                "error", error,
                "message", message
        ));
    }

    // -------------------------
    // Legacy Handlers
    // -------------------------

    private class LegacyRunHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                writeError(exchange, 405, "method_not_allowed", "Use POST");
                return;
            }
            if (!config.isEnabledRun()) {
                writeError(exchange, 403, "forbidden", "run disabled");
                return;
            }
            if (!isAuthorized(exchange)) {
                writeError(exchange, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            String command = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (command.isEmpty()) {
                writeError(exchange, 400, "bad_request", "empty command");
                return;
            }

            logger.log("INFO", "Executing command via /run: " + command);
            commandExecutor.execute(command);

            writeJson(exchange, 200, Json.obj("ok", true));
        }
    }

    private class LegacyLogHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                writeError(exchange, 405, "method_not_allowed", "Use POST");
                return;
            }
            if (!config.isEnabledLog()) {
                writeError(exchange, 403, "forbidden", "log disabled");
                return;
            }
            if (!isAuthorized(exchange)) {
                writeError(exchange, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            String message = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (message.isEmpty()) {
                writeError(exchange, 400, "bad_request", "empty message");
                return;
            }

            logger.log("INFO", "[HungerBridge] " + message);
            writeJson(exchange, 200, Json.obj("ok", true));
        }
    }

    // -------------------------
    // JSON v1 Handlers
    // -------------------------

    private class RunV1Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                writeError(exchange, 405, "method_not_allowed", "Use POST");
                return;
            }
            if (!config.isEnabledRun()) {
                writeError(exchange, 403, "forbidden", "run disabled");
                return;
            }
            if (!isAuthorized(exchange)) {
                writeError(exchange, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            JsonObject json = readJson(exchange);
            if (json == null || !json.has("command")) {
                writeError(exchange, 400, "bad_request", "Missing field: command");
                return;
            }

            String command = json.get("command").getAsString();
            boolean silent = json.has("silent") && json.get("silent").getAsBoolean();

            logger.log("INFO", "Executing command via /v1/run: " + command);

            // Paper will capture output; Fabric will ignore it
            List<String> output = commandExecutor.executeWithOutput(command);

            JsonObject response = Json.obj("ok", true);
            if (!silent && output != null) {
                response.add("output", Json.GSON.toJsonTree(output));
            }

            writeJson(exchange, 200, response);
        }
    }

    private class LogV1Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                writeError(exchange, 405, "method_not_allowed", "Use POST");
                return;
            }
            if (!config.isEnabledLog()) {
                writeError(exchange, 403, "forbidden", "log disabled");
                return;
            }
            if (!isAuthorized(exchange)) {
                writeError(exchange, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            JsonObject json = readJson(exchange);
            if (json == null || !json.has("message")) {
                writeError(exchange, 400, "bad_request", "Missing field: message");
                return;
            }

            String level = json.has("level") ? json.get("level").getAsString() : "info";
            String message = json.get("message").getAsString();

            logger.log(level.toUpperCase(), message);
            writeJson(exchange, 200, Json.obj("ok", true));
        }
    }

    private class StatusV1Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            writeJson(exchange, 200, Json.obj(
                    "ok", true,
                    "bridge", config.getVersion(),
                    "platform", config.getPlatform(),
                    "minecraft", config.getMinecraftVersion()
            ));
        }
    }

    private class VersionV1Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            writeJson(exchange, 200, Json.obj(
                    "bridge", config.getVersion(),
                    "platform", config.getPlatform(),
                    "minecraft", config.getMinecraftVersion()
            ));
        }
    }
}
