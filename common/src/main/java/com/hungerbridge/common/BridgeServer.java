package com.hungerbridge.common;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Minimal HTTP server exposing POST /log
 * Body is treated as plain text and forwarded to the provided Logger.
 */
public class BridgeServer {

    private final Config config;
    private final Logger logger;
    private HttpServer http;

    public BridgeServer(Config config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void start() throws IOException {
        http = HttpServer.create(new InetSocketAddress(config.port), 0);
        http.createContext("/log", new LogHandler());
        http.setExecutor(Executors.newCachedThreadPool());
        http.start();
        logger.log("info", "HungerBridge HTTP /log listening on port " + config.port);
    }

    public void stop() {
        if (http != null) {
            http.stop(0);
            http = null;
            logger.log("info", "HungerBridge HTTP server stopped");
        }
    }

    private class LogHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
                String body = new String(bodyBytes, StandardCharsets.UTF_8).trim();
                if (body.isEmpty()) body = "(empty log message)";

                logger.log("info", body);

                byte[] resp = "ok".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(200, resp.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resp);
                }
            } catch (Exception e) {
                logger.log("error", "Error handling /log: " + e.getMessage());
                try { exchange.sendResponseHeaders(500, -1); } catch (Exception ignored) {}
            } finally {
                exchange.close();
            }
        }
    }
}
