package com.hungerbridge.common;

import com.hungerbridge.common.util.Platform;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * HTTP server exposing:
 *  - POST /run  : body = command string, response = command output
 *  - POST /log  : body = log message, response = "ok"
 */
public class BridgeServer {

    private final Config config;
    private final Platform.CommandExecutor executor;
    private final Platform.Logger logger;
    private HttpServer http;

    public BridgeServer(Config config,
                        Platform.CommandExecutor executor,
                        Platform.Logger logger) {
        this.config = config;
        this.executor = executor;
        this.logger = logger;
    }

    public void start() throws IOException {
        http = HttpServer.create(new InetSocketAddress(config.port), 0);
        http.createContext("/run", new RunHandler());
        http.createContext("/log", new LogHandler());
        http.setExecutor(Executors.newCachedThreadPool());
        http.start();
    }

    public void stop() {
        if (http != null) {
            http.stop(0);
        }
    }

    private class RunHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String cmd = readBody(exchange);
            if (cmd == null || cmd.isBlank()) {
                sendText(exchange, 400, "missing command");
                return;
            }

            String output;
            try {
                output = executor.execute(cmd);
            } catch (Exception e) {
                logger.log("error", "Command execution failed: " + e.getMessage());
                sendText(exchange, 500, "error");
                return;
            }

            sendText(exchange, 200, output == null ? "" : output);
        }
    }

    private class LogHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String msg = readBody(exchange);
            if (msg == null) msg = "";
            logger.log("info", msg);
            sendText(exchange, 200, "ok");
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            byte[] bytes = in.readAllBytes();
            if (bytes.length == 0) return "";
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private static void sendText(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
