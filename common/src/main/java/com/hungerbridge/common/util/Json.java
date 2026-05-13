package com.hungerbridge.common.util;

import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Json {

    private static final Gson GSON = new GsonBuilder().create();

    public static JsonObject parse(String s) {
        return JsonParser.parseString(s).getAsJsonObject();
    }

    public static String stringify(Object o) {
        return GSON.toJson(o);
    }

    public static String readBody(InputStream in) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
