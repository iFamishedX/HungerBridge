package com.hungerbridge.common.endpoints;

import com.hungerbridge.common.Config;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RootHandler implements HttpHandler {

    private final Config config;

    public RootHandler(Config config) {
        this.config = config;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // default root response
        String json = """
        {
          "ok": true,
          "service": "HungerBridge"
        }
        """;
        sendJson(exchange, 200, json);
    }

    public HttpHandler wrap(HttpHandler inner) {
        return exchange -> {
            try {
                if (!checkAuth(exchange)) {
                    String json = """
                    {
                      "ok": false,
                      "error": "unauthorized"
                    }
                    """;
                    sendJson(exchange, 401, json);
                    return;
                }

                inner.handle(exchange);

            } catch (Exception e) {
                String json = """
                {
                  "ok": false,
                  "error": "%s"
                }
                """.formatted(safe(e.getMessage()));
                sendJson(exchange, 500, json);
            }
        };
    }

    private boolean checkAuth(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null) return false;
        String expected = "Bearer " + config.auth.key;
        return auth.equals(expected);
    }

    public static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
