package com.hungerbridge.common.endpoints;

import com.hungerbridge.common.util.Platform;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Run implements HttpHandler {

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

        String command = "";
        boolean silent = false;

        int cmdIdx = body.indexOf("\"command\"");
        if (cmdIdx >= 0) {
            int colon = body.indexOf(":", cmdIdx);
            int q1 = body.indexOf("\"", colon + 1);
            int q2 = body.indexOf("\"", q1 + 1);
            command = body.substring(q1 + 1, q2);
        }

        int silentIdx = body.indexOf("\"silent\"");
        if (silentIdx >= 0) {
            int colon = body.indexOf(":", silentIdx);
            int comma = body.indexOf(",", colon);
            if (comma < 0) comma = body.indexOf("}", colon);
            String val = body.substring(colon + 1, comma).trim();
            silent = val.equalsIgnoreCase("true");
        }

        String result = Platform.executor().run(command, silent);

        String json = """
        {
          "ok": true,
          "command": "%s",
          "result": "%s"
        }
        """.formatted(escape(command), escape(result));

        RootHandler.sendJson(exchange, 200, json);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
