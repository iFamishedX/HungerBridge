package com.hungerbridge.common;

import com.google.gson.JsonObject;
import com.hungerbridge.common.endpoints.Log;
import com.hungerbridge.common.endpoints.Ping;
import com.hungerbridge.common.endpoints.Run;
import com.hungerbridge.common.util.Json;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BridgeHandler implements com.sun.net.httpserver.HttpHandler {

    private final Config config;
    private final Auth auth;

    public BridgeHandler(Config config) {
        this.config = config;
        this.auth = new Auth(config);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!auth.check(exchange)) {
                send(exchange, 401, "{\"ok\":false,\"error\":\"unauthorized\"}");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if ("/ping".equals(path) && "GET".equalsIgnoreCase(method)) {
                if (!config.enabled_endpoints.ping) {
                    send(exchange, 404, "{\"ok\":false,\"error\":\"disabled\"}");
                    return;
                }
                send(exchange, 200, Json.stringify(Ping.handle()));
                return;
            }

            if ("/run".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!config.enabled_endpoints.run) {
                    send(exchange, 404, "{\"ok\":false,\"error\":\"disabled\"}");
                    return;
                }
                String body = Json.readBody(exchange.getRequestBody());
                JsonObject obj = Json.parse(body);
                send(exchange, 200, Json.stringify(Run.handle(obj)));
                return;
            }

            if ("/log".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!config.enabled_endpoints.log) {
                    send(exchange, 404, "{\"ok\":false,\"error\":\"disabled\"}");
                    return;
                }
                String body = Json.readBody(exchange.getRequestBody());
                JsonObject obj = Json.parse(body);
                send(exchange, 200, Json.stringify(Log.handle(obj)));
                return;
            }

            send(exchange, 404, "{\"ok\":false,\"error\":\"not_found\"}");
        } catch (Exception e) {
            e.printStackTrace();
            send(exchange, 500, "{\"ok\":false,\"error\":\"internal\"}");
        }
    }

    private void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
