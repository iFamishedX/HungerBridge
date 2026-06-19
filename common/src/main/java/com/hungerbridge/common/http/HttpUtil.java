package com.hungerbridge.common.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hungerbridge.common.Config;
import com.hungerbridge.common.Json;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Small HTTP utilities shared by handlers.
 */
public final class HttpUtil {

    private HttpUtil() {}

    public static boolean auth(HttpExchange ex, Config config) {
        String key = ex.getRequestHeaders().getFirst("X-Auth-Key");
        return key != null && key.equals(config.getAuthKey());
    }

    public static JsonObject readJson(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            String body = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
            if (body.isEmpty()) return null;
            return JsonParser.parseString(body).getAsJsonObject();
        }
    }

    public static void writeJson(HttpExchange ex, int status, JsonObject body) throws IOException {
        byte[] bytes = Json.stringify(body).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    public static void error(HttpExchange ex, int status, String err, String msg, Config config) throws IOException {
        writeJson(ex, status, Json.obj(
                "ok", false,
                "error", err,
                "message", msg
        ));
    }
}
