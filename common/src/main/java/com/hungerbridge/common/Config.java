package com.hungerbridge.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Config {

    public final int port;

    public static class Auth {
        public final boolean enabled;
        public final String key;

        public Auth(boolean enabled, String key) {
            this.enabled = enabled;
            this.key = key;
        }
    }

    public static class Endpoints {
        public final boolean ping;
        public final boolean run;
        public final boolean log;

        public Endpoints(boolean ping, boolean run, boolean log) {
            this.ping = ping;
            this.run = run;
            this.log = log;
        }
    }

    public final Auth auth;
    public final Endpoints enabled_endpoints;

    public Config(int port, Auth auth, Endpoints endpoints) {
        this.port = port;
        this.auth = auth;
        this.enabled_endpoints = endpoints;
    }

    public static Config load(Path dir) throws IOException {
        if (!Files.exists(dir)) Files.createDirectories(dir);

        Path file = dir.resolve("hungerbridge.yml");

        if (!Files.exists(file)) {
            String def = """
            port: 1913
            auth:
              enabled: true
              key: CHANGE_ME
            enabled_endpoints:
              ping: true
              run: true
              log: true
            """;
            Files.writeString(file, def);
        }

        List<String> lines = Files.readAllLines(file);

        int port = 1913;
        boolean authEnabled = true;
        String authKey = "CHANGE_ME";
        boolean epPing = true;
        boolean epRun = true;
        boolean epLog = true;

        String section = "";

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.endsWith(":")) {
                section = line.substring(0, line.length() - 1).trim();
                continue;
            }

            int colon = line.indexOf(":");
            if (colon < 0) continue;

            String key = line.substring(0, colon).trim();
            String val = line.substring(colon + 1).trim();

            switch (section) {
                case "" -> {
                    if (key.equals("port")) port = Integer.parseInt(val);
                }
                case "auth" -> {
                    if (key.equals("enabled")) authEnabled = Boolean.parseBoolean(val);
                    if (key.equals("key")) authKey = val;
                }
                case "enabled_endpoints" -> {
                    if (key.equals("ping")) epPing = Boolean.parseBoolean(val);
                    if (key.equals("run")) epRun = Boolean.parseBoolean(val);
                    if (key.equals("log")) epLog = Boolean.parseBoolean(val);
                }
            }
        }

        return new Config(
                port,
                new Auth(authEnabled, authKey),
                new Endpoints(epPing, epRun, epLog)
        );
    }
}
