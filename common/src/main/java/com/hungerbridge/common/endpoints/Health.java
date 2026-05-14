package com.hungerbridge.common.endpoints;

public class Health {

    private static final long START = System.currentTimeMillis();

    public static String handle() {
        long uptime = System.currentTimeMillis() - START;

        return """
        {
          "ok": true,
          "status": "ok",
          "uptime": %d
        }
        """.formatted(uptime);
    }
}
