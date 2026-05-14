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
        if (http != null) http.stop(0);
    }

    private class RunHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(405, -1);
                return;
            }
            String cmd = readBody(ex);
            if (cmd == null || cmd.isBlank()) {
                sendText(ex, 400, "missing command");
                return;
            }
            String result;
            try {
                result = executor.execute(cmd);
            } catch (Exception e) {
                logger.log("error", "Command failed: " + e.getMessage());
                sendText(ex, 500, "error");
                return;
            }
            sendText(ex, 200, result == null ? "" : result);
        }
    }

    private class LogHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(405, -1);
                return;
            }
            String msg = readBody(ex);
            if (msg == null) msg = "";
            logger.log("info", msg);
            sendText(ex, 200, "ok");
        }
    }

    private static String readBody(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            byte[] bytes = in.readAllBytes();
            if (bytes.length == 0) return "";
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private static void sendText(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
