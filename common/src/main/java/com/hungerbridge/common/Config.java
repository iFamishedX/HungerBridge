package com.hungerbridge.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Minimal config holder. Stores/loads a single property: port.
 */
public final class Config {
    public final int port;

    private Config(int port) {
        this.port = port;
    }

    public static Config load(Path dir) throws IOException {
        Files.createDirectories(dir);
        Path file = dir.resolve("hungerbridge.properties");
        int port = 8080;

        if (Files.exists(file)) {
            Properties p = new Properties();
            try (var in = Files.newInputStream(file)) {
                p.load(in);
            }
            String v = p.getProperty("port");
            if (v != null) {
                try {
                    port = Integer.parseInt(v.trim());
                } catch (NumberFormatException ignored) {}
            }
        } else {
            Properties p = new Properties();
            p.setProperty("port", Integer.toString(port));
            try (var out = Files.newOutputStream(file)) {
                p.store(out, "HungerBridge config");
            }
        }

        return new Config(port);
    }
}
