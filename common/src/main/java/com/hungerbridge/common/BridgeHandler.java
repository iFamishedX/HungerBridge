package com.hungerbridge.common;

import com.hungerbridge.common.endpoints.Health;
import com.hungerbridge.common.endpoints.Log;
import com.hungerbridge.common.endpoints.Run;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BridgeHandler implements HttpHandler {

    private final Config config;

    public BridgeHandler(Config config) {
        this.config = config;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!Auth.check(config, exchange)) {
            send(exchange, 401, """
            { "ok": false, "error": "unauthorized" }
            """);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        switch (path) {
            case "/ping" -> {
                if (!config.enabled_endpoints.ping) {
                    send(exchange, 403, """{ "ok": false, "error": "disabled" }""");
                    return;
                }
                send(exchange, 200, """{ "ok": true, "pong": true }""");
            }

            case "/run" -> {
                if (!config.enabled_endpoints.run) {
                    send(exchange, 403, """{ "ok": false, "error": "disabled" }""");
                    return;
                }
                send(exchange, 200, Run.handle(exchange));
            }

            case "/log" -> {
                if (!config.enabled_endpoints.log) {
                    send(exchange, 403, """{ "ok": false, "error": "disabled" }""");
                    return;
                }
                send(exchange, 200, Log.handle(exchange));
            }

            case "/health" -> {
                send(exchange, 200, Health.handle());
            }

            default -> {
                send(exchange, 404, """{ "ok": false, "error": "not_found" }""");
            }
        }
    }

    private void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }
}
