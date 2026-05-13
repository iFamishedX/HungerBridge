package com.hungerbridge.common.endpoints;

import com.google.gson.JsonObject;
import com.hungerbridge.common.util.Json;
import com.hungerbridge.common.util.Platform;

public class Run {

    public static JsonObject handle(JsonObject body) {
        String command = body.has("command") ? body.get("command").getAsString() : "";
        boolean silent = body.has("silent") && body.get("silent").getAsBoolean();

        JsonObject res = new JsonObject();
        if (command.isEmpty()) {
            res.addProperty("ok", false);
            res.addProperty("error", "Missing command");
            return res;
        }

        try {
            String output = Platform.commandRunner().run(command, silent);
            res.addProperty("ok", true);
            res.addProperty("output", output == null ? "" : output);
        } catch (Exception e) {
            res.addProperty("ok", false);
            res.addProperty("error", e.getMessage());
        }
        return res;
    }
}
