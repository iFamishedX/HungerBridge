package com.hungerbridge.common;

import com.sun.net.httpserver.HttpExchange;

public class Auth {

    public static boolean check(Config config, HttpExchange exchange) {
        if (!config.auth.enabled) return true;

        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header == null) return false;

        return header.equals("Bearer " + config.auth.key);
    }
}
