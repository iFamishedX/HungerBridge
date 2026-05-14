package com.hungerbridge.common;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
// import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Configuration holder for HungerBridge.
 * Responsible for auto-generating and loading config.yaml.
 */
public final class Config {

    private final int port;
    private final String authKey;
    private final boolean enabledRun;
    private final boolean enabledLog;

    public Config(int port, String authKey, boolean enabledRun, boolean enabledLog) {
        this.port = port;
        this.authKey = authKey;
        this.enabledRun = enabledRun;
        this.enabledLog = enabledLog;
    }

    public int getPort() {
        return port;
    }

    public String getAuthKey() {
        return authKey;
    }

    public boolean isEnabledRun() {
        return enabledRun;
    }

    public boolean isEnabledLog() {
        return enabledLog;
    }

    /**
     * Load configuration from the given directory, generating a default config.yaml if missing.
     *
     * @param configDir directory where config.yaml should live
     * @param logger    logger for informational messages
     * @return loaded Config instance
     */
    @SuppressWarnings("unchecked")
    public static Config load(Path configDir, Logger logger) {
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Path configFile = configDir.resolve("config.yaml");

            if (!Files.exists(configFile)) {
                logger.log("INFO", "Config file not found, generating default config at " + configFile);

                Map<String, Object> root = new HashMap<>();
                root.put("port", 8080);

                Map<String, Object> auth = new HashMap<>();
                auth.put("key", UUID.randomUUID().toString());
                root.put("auth", auth);

                Map<String, Object> enabled = new HashMap<>();
                enabled.put("run", Boolean.TRUE);
                enabled.put("log", Boolean.TRUE);
                root.put("enabled", enabled);

                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                options.setPrettyFlow(true);
                Yaml yaml = new Yaml(options);

                try (OutputStream out = Files.newOutputStream(configFile)) {
                    yaml.dump(root, new java.io.OutputStreamWriter(out));
                }
            }

            Yaml yaml = new Yaml();
            Map<String, Object> root;
            try (InputStream in = Files.newInputStream(configFile)) {
                Object loaded = yaml.load(in);
                if (!(loaded instanceof Map)) {
                    throw new IllegalStateException("Invalid config.yaml structure");
                }
                root = (Map<String, Object>) loaded;
            }

            int port = ((Number) root.getOrDefault("port", 8080)).intValue();

            Map<String, Object> auth = (Map<String, Object>) root.getOrDefault("auth", new HashMap<>());
            String authKey = (String) auth.getOrDefault("key", "");

            Map<String, Object> enabled = (Map<String, Object>) root.getOrDefault("enabled", new HashMap<>());
            boolean enabledRun = (Boolean) enabled.getOrDefault("run", Boolean.TRUE);
            boolean enabledLog = (Boolean) enabled.getOrDefault("log", Boolean.TRUE);

            if (authKey == null || authKey.isEmpty()) {
                logger.log("WARN", "auth.key is empty in config.yaml; HTTP endpoints will be unusable until set.");
            }

            return new Config(port, authKey, enabledRun, enabledLog);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load HungerBridge config", e);
        }
    }
}
