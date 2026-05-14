package com.hungerbridge.common.endpoints;

import com.hungerbridge.common.util.Platform;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Log implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String json = """
            {
              "ok": false,
              "error": "method_not_allowed"
            }
            """;
            RootHandler.sendJson(exchange, 405, json);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();

        String level = "info";
        String message = "";

        int lvlIdx = body.indexOf("\"level\"");
        if (lvlIdx >= 0) {
            int colon = body.indexOf(":", lvlIdx);
            int q1 = body.indexOf("\"", colon + 1);
            int q2 = body.indexOf("\"", q1 + 1);
            level = body.substring(q1 + 1, q2);
        }

        int msgIdx = body.indexOf("\"message\"");
        if (msgIdx >= 0) {
            int colon = body.indexOf(":", msgIdx);
            int q1 = body.indexOf("\"", colon + 1);
            int q2 = body.indexOf("\"", q1 + 1);
            message = body.substring(q1 + 1, q2);
        }

        Platform.logger().log(level, message);

        String json = """
        {
          "ok": true,
          "level": "%s",
          "message": "%s"
        }
        """.formatted(escape(level), escape(message));

        RootHandler.sendJson(exchange, 200, json);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
