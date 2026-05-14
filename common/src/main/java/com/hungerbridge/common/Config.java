package com.hungerbridge.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {

    public final int port;

    private Config(int port) {
        this.port = port;
    }

    public static Config load(Path cfgDir) throws IOException {
        Files.createDirectories(cfgDir);

        Path file = cfgDir.resolve("hungerbridge.properties");
        int port = 8080;

        if (Files.exists(file)) {
            Properties p = new Properties();
            try (var in = Files.newInputStream(file)) {
                p.load(in);
            }
            String portStr = p.getProperty("port");
            if (portStr != null) {
                try {
                    port = Integer.parseInt(portStr.trim());
                } catch (NumberFormatException ignored) {}
            }
        } else {
            Properties p = new Properties();
            p.setProperty("port", Integer.toString(port));
            try (var out = Files.newOutputStream(file)) {
                p.store(out, "HungerBridge configuration");
            }
        }

        return new Config(port);
    }
}
