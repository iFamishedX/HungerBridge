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

/**
 * HTTP bridge server used by both Fabric and Paper modules.
 * Provides legacy, v1, and v2 endpoints.
 */
public final class BridgeServer {

    private final Config config;
    private final Logger logger;
    private final CommandExecutor executor;

    private HttpServer server;
    private ExecutorService pool;

    public BridgeServer(Config config, Logger logger, CommandExecutor executor) {
        this.config = config;
        this.logger = logger;
        this.executor = executor;
    }

    public synchronized void start() {
        if (server != null) return;

        try {
            server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
        } catch (IOException e) {
            throw new RuntimeException("Failed to bind HTTP server", e);
        }

        pool = Executors.newCachedThreadPool();
        server.setExecutor(pool);

        // Legacy
        server.createContext("/run", new LegacyRun());
        server.createContext("/log", new LegacyLog());

        // JSON v1
        server.createContext("/v1/run", new RunV1());
        server.createContext("/v1/log", new LogV1());
        server.createContext("/v1/status", new StatusV1());
        server.createContext("/v1/version", new VersionV1());

        // JSON v2
        server.createContext("/v2/ping", new PingV2());
        server.createContext("/v2/info", new InfoV2());
        server.createContext("/v2/status", new StatusV2());
        server.createContext("/v2/run", new RunV2());
        server.createContext("/v2/log", new LogV2());
        server.createContext("/v2/tps", new TpsV2());
        server.createContext("/v2/players", new PlayersV2());

        server.start();
        logger.log("INFO", "HungerBridge HTTP server started on port " + config.getPort());
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        if (pool != null) {
            pool.shutdownNow();
            pool = null;
        }
        logger.log("INFO", "HungerBridge HTTP server stopped.");
    }

    // helpers

    private boolean auth(HttpExchange ex) {
        String key = ex.getRequestHeaders().getFirst("X-Auth-Key");
        return key != null && key.equals(config.getAuthKey());
    }

    private JsonObject readJson(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            String body = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
            if (body.isEmpty()) return null;
            return JsonParser.parseString(body).getAsJsonObject();
        }
    }

    private void writeJson(HttpExchange ex, int status, JsonObject body) throws IOException {
        byte[] bytes = Json.stringify(body).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    private void error(HttpExchange ex, int status, String err, String msg) throws IOException {
        writeJson(ex, status, Json.obj(
                "ok", false,
                "error", err,
                "message", msg
        ));
    }

    // legacy handlers

    private class LegacyRun implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!config.isLegacyRunEnabled()) {
                error(ex, 403, "forbidden", "legacy run disabled");
                return;
            }
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                error(ex, 405, "method_not_allowed", "Use POST");
                return;
            }
            if (!auth(ex)) {
                error(ex, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            String cmd = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (cmd.isEmpty()) {
                error(ex, 400, "bad_request", "empty command");
                return;
            }
            executor.execute(cmd);

            writeJson(ex, 200, Json.obj("ok", true));
        }
    }

    private class LegacyLog implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!config.isLegacyLogEnabled()) {
                error(ex, 403, "forbidden", "legacy log disabled");
                return;
            }
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                error(ex, 405, "method_not_allowed", "Use POST");
                return;
            }
            if (!auth(ex)) {
                error(ex, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            String msg = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (msg.isEmpty()) {
                error(ex, 400, "bad_request", "empty message");
                return;
            }

            logger.log("INFO", msg);
            writeJson(ex, 200, Json.obj("ok", true));
        }
    }

    // v1 handlers

    private class RunV1 implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!config.isV1RunEnabled()) {
                error(ex, 403, "forbidden", "v1 run disabled");
                return;
            }
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                error(ex, 405, "method_not_allowed", "Use POST");
                return;
            }
            if (!auth(ex)) {
                error(ex, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            JsonObject json = readJson(ex);
            if (json == null || !json.has("command")) {
                error(ex, 400, "bad_request", "Missing field: command");
                return;
            }

            String cmd = json.get("command").getAsString();
            boolean silent = json.has("silent") && json.get("silent").getAsBoolean();
            boolean showConsole = json.has("show_console") && json.get("show_console").getAsBoolean();

            List<String> out = executor.executeWithOutput(cmd, showConsole);

            JsonObject resp = Json.obj("ok", true);
            if (!silent && out != null) {
                resp.add("output", Json.GSON.toJsonTree(out));
            }

            writeJson(ex, 200, resp);
        }
    }

    private class LogV1 implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!config.isV1LogEnabled()) {
                error(ex, 403, "forbidden", "v1 log disabled");
                return;
            }
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                error(ex, 405, "method_not_allowed", "Use POST");
                return;
            }
            if (!auth(ex)) {
                error(ex, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            JsonObject json = readJson(ex);
            if (json == null || !json.has("message")) {
                error(ex, 400, "bad_request", "Missing field: message");
                return;
            }

            String level = json.has("level") ? json.get("level").getAsString() : "info";
            String msg = json.get("message").getAsString();

            logger.log(level.toUpperCase(), msg);
            writeJson(ex, 200, Json.obj("ok", true));
        }
    }

    private class StatusV1 implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!config.isV1StatusEnabled()) {
                error(ex, 403, "forbidden", "v1 status disabled");
                return;
            }

            writeJson(ex, 200, Json.obj(
                    "ok", true,
                    "bridge", config.getVersion(),
                    "platform", config.getPlatform(),
                    "minecraft", config.getMinecraftVersion()
            ));
        }
    }

    private class VersionV1 implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!config.isV1VersionEnabled()) {
                error(ex, 403, "forbidden", "v1 version disabled");
                return;
            }

            writeJson(ex, 200, Json.obj(
                    "bridge", config.getVersion(),
                    "platform", config.getPlatform(),
                    "minecraft", config.getMinecraftVersion()
            ));
        }
    }

    // v2 handlers

    private class PingV2 implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!config.isV2PingEnabled()) {
                error(ex, 403, "forbidden", "v2 ping disabled");
                return;
            }

            long start = System.nanoTime();

            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                error(ex, 405, "method_not_allowed", "Use GET");
                return;
            }
            if (!auth(ex)) {
                error(ex, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            long end = System.nanoTime();
            long latencyMs = (end - start) / 1_000_000L;

            JsonObject resp = Json.obj(
                    "ok", true,
                    "latency_ms", latencyMs
            );
            writeJson(ex, 200, resp);
        }
    }

    private class InfoV2 implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!config.isV2InfoEnabled()) {
                error(ex, 403, "forbidden", "v2 info disabled");
                return;
            }

            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                error(ex, 405, "method_not_allowed", "Use GET");
                return;
            }
            if (!auth(ex)) {
                error(ex, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            JsonObject bridge = Json.obj(
                    "version", config.getVersion(),
                    "platform", config.getPlatform(),
                    "minecraft", config.getMinecraftVersion()
            );

            JsonObject resp = Json.obj(
                    "ok", true,
                    "bridge", bridge
            );

            writeJson(ex, 200, resp);
        }
    }

    private class StatusV2 implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!config.isV2StatusEnabled()) {
                error(ex, 403, "forbidden", "v2 status disabled");
                return;
            }

            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                error(ex, 405, "method_not_allowed", "Use GET");
                return;
            }
            if (!auth(ex)) {
                error(ex, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            JsonObject resp = Json.obj(
                    "ok", true
            );

            writeJson(ex, 200, resp);
        }
    }

    private class RunV2 implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!config.isV2RunEnabled()) {
                error(ex, 403, "forbidden", "v2 run disabled");
                return;
            }
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                error(ex, 405, "method_not_allowed", "Use POST");
                return;
            }
            if (!auth(ex)) {
                error(ex, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            JsonObject json = readJson(ex);
            if (json == null || !json.has("command")) {
                error(ex, 400, "bad_request", "Missing field: command");
                return;
            }

            String cmd = json.get("command").getAsString();
            boolean silent = json.has("silent") && json.get("silent").getAsBoolean();
            boolean showConsole = json.has("show_console") && json.get("show_console").getAsBoolean();

            List<String> out = executor.executeWithOutput(cmd, showConsole);

            JsonObject resp = Json.obj("ok", true);
            if (!silent && out != null) {
                resp.add("output", Json.GSON.toJsonTree(out));
            }

            writeJson(ex, 200, resp);
        }
    }

    private class LogV2 implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!config.isV2LogEnabled()) {
                error(ex, 403, "forbidden", "v2 log disabled");
                return;
            }
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                error(ex, 405, "method_not_allowed", "Use POST");
                return;
            }
            if (!auth(ex)) {
                error(ex, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            JsonObject json = readJson(ex);
            if (json == null || !json.has("message")) {
                error(ex, 400, "bad_request", "Missing field: message");
                return;
            }

            String level = json.has("level") ? json.get("level").getAsString() : "info";
            String msg = json.get("message").getAsString();

            logger.log(level.toUpperCase(), msg);
            writeJson(ex, 200, Json.obj("ok", true));
        }
    }

    private class TpsV2 implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!config.isV2TpsEnabled()) {
                error(ex, 403, "forbidden", "v2 tps disabled");
                return;
            }
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                error(ex, 405, "method_not_allowed", "Use GET");
                return;
            }
            if (!auth(ex)) {
                error(ex, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            JsonObject resp = Json.obj(
                    "ok", true,
                    "tps", executor.getTps(),
                    "tps_1m", executor.getTps1m(),
                    "tps_5m", executor.getTps5m(),
                    "tps_15m", executor.getTps15m(),
                    "tick_time_ms", executor.getTickTimeMs()
            );

            writeJson(ex, 200, resp);
        }
    }

    private class PlayersV2 implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!config.isV2PlayersEnabled()) {
                error(ex, 403, "forbidden", "v2 players disabled");
                return;
            }
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                error(ex, 405, "method_not_allowed", "Use GET");
                return;
            }
            if (!auth(ex)) {
                error(ex, 401, "unauthorized", "Invalid X-Auth-Key");
                return;
            }

            int max = config.getPlayersMaxList();

            List<String> names = executor.getOnlinePlayerNames();
            int count = names.size();

            if (names.size() > max) {
                names = names.subList(0, max);
            }

            JsonObject resp = Json.obj(
                    "ok", true,
                    "count", count,
                    "players", Json.GSON.toJsonTree(names)
            );

            writeJson(ex, 200, resp);
        }
    }
}
