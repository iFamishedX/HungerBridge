package com.hungerbridge.common.endpoints;

import com.google.gson.JsonObject;
import com.hungerbridge.common.util.Platform;

public class Log {

    public static JsonObject handle(JsonObject body) {
        String message = body.has("message") ? body.get("message").getAsString() : "";
        String level = body.has("level") ? body.get("level").getAsString() : "info";

        JsonObject res = new JsonObject();
        if (message.isEmpty()) {
            res.addProperty("ok", false);
            res.addProperty("error", "Missing message");
            return res;
        }

        Platform.logger().log(level, message);
        res.addProperty("ok", true);
        return res;
    }
}
