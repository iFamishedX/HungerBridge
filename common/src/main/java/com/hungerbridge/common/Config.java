package com.hungerbridge.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Config {

    public final int port;
    public final String token;

    public Config(int port, String token) {
        this.port = port;
        this.token = token;
    }

    public static Config load(Path configDir) throws IOException {
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }

        Path file = configDir.resolve("hungerbridge.yml");

        if (!Files.exists(file)) {
            String defaultYaml = """
            port: 1913
            token: CHANGE_ME
            """;
            Files.writeString(file, defaultYaml);
            return new Config(1913, "CHANGE_ME");
        }

        List<String> lines = Files.readAllLines(file);

        int port = 1913;
        String token = "CHANGE_ME";

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int colon = line.indexOf(':');
            if (colon < 0) continue;

            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();

            if (key.equalsIgnoreCase("port")) {
                try {
                    port = Integer.parseInt(value);
                } catch (NumberFormatException ignored) {
                }
            } else if (key.equalsIgnoreCase("token")) {
                token = value;
            }
        }

        return new Config(port, token);
    }
}
