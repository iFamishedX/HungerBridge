package com.hungerbridge.common.endpoints;

import com.google.gson.JsonObject;

public class Ping {

    public static JsonObject handle() {
        JsonObject res = new JsonObject();
        res.addProperty("ok", true);
        return res;
    }
}
