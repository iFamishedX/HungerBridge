package com.hungerbridge.common.endpoints;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class Health implements HttpHandler {

    private final long startTime = System.currentTimeMillis();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        long uptime = System.currentTimeMillis() - startTime;

        String json = """
        {
          "ok": true,
          "status": "ok",
          "uptime": %d
        }
        """.formatted(uptime);

        RootHandler.sendJson(exchange, 200, json);
    }
}
