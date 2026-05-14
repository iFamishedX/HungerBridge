package com.hungerbridge.common.endpoints;

import com.hungerbridge.common.util.Platform;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Run {

    public static String handle(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        String cmd = extract(body, "command");
        boolean silent = Boolean.parseBoolean(extract(body, "silent"));

        String result = Platform.executor().run(cmd);

        return """
        {
          "ok": true,
          "command": "%s",
          "result": "%s"
        }
        """.formatted(escape(cmd), escape(result));
    }

    private static String extract(String body, String key) {
        int idx = body.indexOf("\"" + key + "\"");
        if (idx < 0) return "";
        int colon = body.indexOf(":", idx);
        int q1 = body.indexOf("\"", colon + 1);
        int q2 = body.indexOf("\"", q1 + 1);
        return body.substring(q1 + 1, q2);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
