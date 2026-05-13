package com.hungerbridge.common;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Config {

    public int port = 1913;

    public static class EnabledEndpoints {
        public boolean run = true;
        public boolean log = true;
        public boolean ping = true;
    }

    public EnabledEndpoints enabled_endpoints = new EnabledEndpoints();

    public static class AuthConfig {
        public boolean enabled = true;
        public String key = "CHANGE_ME";
    }

    public AuthConfig auth = new AuthConfig();

    public static Config load(Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path file = dir.resolve("config.yaml");
            Config cfg = new Config();
            Yaml yaml = new Yaml();

            if (!Files.exists(file)) {
                try (Writer w = Files.newBufferedWriter(file)) {
                    yaml.dump(cfgToMap(cfg), w);
                }
                return cfg;
            }

            try (InputStream in = Files.newInputStream(file)) {
                Map<String, Object> data = yaml.load(in);
                if (data == null) return cfg;

                Object port = data.get("port");
                if (port instanceof Number n) cfg.port = n.intValue();

                Object ee = data.get("enabled_endpoints");
                if (ee instanceof Map<?, ?> m) {
                    Object run = m.get("run");
                    Object log = m.get("log");
                    Object ping = m.get("ping");
                    if (run instanceof Boolean b) cfg.enabled_endpoints.run = b;
                    if (log instanceof Boolean b) cfg.enabled_endpoints.log = b;
                    if (ping instanceof Boolean b) cfg.enabled_endpoints.ping = b;
                }

                Object auth = data.get("auth");
                if (auth instanceof Map<?, ?> m) {
                    Object enabled = m.get("enabled");
                    Object key = m.get("key");
                    if (enabled instanceof Boolean b) cfg.auth.enabled = b;
                    if (key instanceof String s) cfg.auth.key = s;
                }

                return cfg;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new Config();
        }
    }

    private static Map<String, Object> cfgToMap(Config cfg) {
        return Map.of(
                "port", cfg.port,
                "enabled_endpoints", Map.of(
                        "run", cfg.enabled_endpoints.run,
                        "log", cfg.enabled_endpoints.log,
                        "ping", cfg.enabled_endpoints.ping
                ),
                "auth", Map.of(
                        "enabled", cfg.auth.enabled,
                        "key", cfg.auth.key
                )
        );
    }
}
