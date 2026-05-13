package com.hungerbridge.common;

import com.sun.net.httpserver.HttpExchange;

public class Auth {

    private final Config config;

    public Auth(Config config) {
        this.config = config;
    }

    public boolean check(HttpExchange exchange) {
        if (!config.auth.enabled) return true;

        String header = exchange.getRequestHeaders().getFirst("X-HungerBridge-Key");
        if (header == null) return false;

        return header.equals(config.auth.key);
    }
}
