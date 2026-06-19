package com.hungerbridge.common;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Configuration holder for HungerBridge.
 *
 * YAML schema produced / consumed:
 *
 * port: 1913
 *
 * auth:
 *   key: <uuid>
 *
 * endpoints:
 *   run: true
 *   log: true
 *   ping: true
 *   info: true
 *   status: true
 *   tps: true
 *   players: true
 *
 * players:
 *   max-list: 50
 */
public final class Config {

    private final int port;
    private final String authKey;

    // v2 endpoint toggles
    private final boolean runEnabled;
    private final boolean logEnabled;
    private final boolean pingEnabled;
    private final boolean infoEnabled;
    private final boolean statusEnabled;
    private final boolean tpsEnabled;
    private final boolean playersEnabled;

    // Players config
    private final int playersMaxList;

    // JSON API metadata
    private String platform = "unknown";
    private String minecraftVersion = "unknown";
    private String bridgeVersion;

    public Config(
            int port,
            String authKey,
            boolean runEnabled,
            boolean logEnabled,
            boolean pingEnabled,
            boolean infoEnabled,
            boolean statusEnabled,
            boolean tpsEnabled,
            boolean playersEnabled,
            int playersMaxList,
            String bridgeVersion
    ) {
        this.port = port;
        this.authKey = authKey;

        this.runEnabled = runEnabled;
        this.logEnabled = logEnabled;
        this.pingEnabled = pingEnabled;
        this.infoEnabled = infoEnabled;
        this.statusEnabled = statusEnabled;
        this.tpsEnabled = tpsEnabled;
        this.playersEnabled = playersEnabled;

        this.playersMaxList = playersMaxList;
        this.bridgeVersion = bridgeVersion;
    }

    public int getPort() { return port; }
    public String getAuthKey() { return authKey; }

    public boolean isRunEnabled() { return runEnabled; }
    public boolean isLogEnabled() { return logEnabled; }
    public boolean isPingEnabled() { return pingEnabled; }
    public boolean isInfoEnabled() { return infoEnabled; }
    public boolean isStatusEnabled() { return statusEnabled; }
    public boolean isTpsEnabled() { return tpsEnabled; }
    public boolean isPlayersEnabled() { return playersEnabled; }

    public int getPlayersMaxList() { return playersMaxList; }

    public String getVersion() { return bridgeVersion; }
    public String getPlatform() { return platform; }
    public String getMinecraftVersion() { return minecraftVersion; }

    public void setPlatform(String platform) { this.platform = platform; }
    public void setMinecraftVersion(String minecraftVersion) { this.minecraftVersion = minecraftVersion; }
    public void setBridgeVersion(String bridgeVersion) { this.bridgeVersion = bridgeVersion; }

    @SuppressWarnings("unchecked")
    public static Config load(Path configDir, Logger logger) {
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Path configFile = configDir.resolve("config.yaml");

            // Load version.yaml
            String bridgeVersion = "unknown";
            try {
                Path versionFile = configDir.getParent().getParent().resolve("version.yaml");
                if (Files.exists(versionFile)) {
                    Yaml yaml = new Yaml();
                    try (InputStream vin = Files.newInputStream(versionFile)) {
                        Object vloaded = yaml.load(vin);
                        if (vloaded instanceof Map) {
                            Map<String, Object> vroot = (Map<String, Object>) vloaded;
                            bridgeVersion = (String) vroot.getOrDefault("version", "unknown");
                        }
                    }
                }
            } catch (Exception ignored) {}

            // Generate default config
            if (!Files.exists(configFile)) {
                logger.log("WARN", "Config file not found, generating default config at " + configFile);

                Map<String, Object> root = new LinkedHashMap<>();
                root.put("port", 1913);

                Map<String, Object> auth = new LinkedHashMap<>();
                auth.put("key", UUID.randomUUID().toString());
                root.put("auth", auth);

                Map<String, Object> endpoints = new LinkedHashMap<>();
                endpoints.put("run", true);
                endpoints.put("log", true);
                endpoints.put("ping", true);
                endpoints.put("info", true);
                endpoints.put("status", true);
                endpoints.put("tps", true);
                endpoints.put("players", true);
                root.put("endpoints", endpoints);

                Map<String, Object> players = new LinkedHashMap<>();
                players.put("max-list", 50);
                root.put("players", players);

                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                options.setPrettyFlow(true);
                Yaml yaml = new Yaml(options);

                String dumped = yaml.dump(root);

                // Insert blank lines between sections
                String spaced = dumped
                        .replace("\nauth:", "\n\nauth:")
                        .replace("\nendpoints:", "\n\nendpoints:")
                        .replace("\nplayers:", "\n\nplayers:");

                try (OutputStream out = Files.newOutputStream(configFile);
                     OutputStreamWriter writer = new OutputStreamWriter(out)) {
                    writer.write(spaced);
                }
            }

            // Load config.yaml
            Yaml yaml = new Yaml();
            Map<String, Object> root;
            try (InputStream in = Files.newInputStream(configFile)) {
                Object loaded = yaml.load(in);
                if (!(loaded instanceof Map)) {
                    throw new IllegalStateException("Invalid config.yaml structure");
                }
                root = (Map<String, Object>) loaded;
            }

            int port = ((Number) root.getOrDefault("port", 1913)).intValue();

            Map<String, Object> auth = (Map<String, Object>) root.getOrDefault("auth", new LinkedHashMap<>());
            String authKey = (String) auth.getOrDefault("key", "");

            Map<String, Object> endpoints = (Map<String, Object>) root.getOrDefault("endpoints", new LinkedHashMap<>());
            Map<String, Object> players = (Map<String, Object>) root.getOrDefault("players", new LinkedHashMap<>());

            boolean run = (Boolean) endpoints.getOrDefault("run", true);
            boolean log = (Boolean) endpoints.getOrDefault("log", true);
            boolean ping = (Boolean) endpoints.getOrDefault("ping", true);
            boolean info = (Boolean) endpoints.getOrDefault("info", true);
            boolean status = (Boolean) endpoints.getOrDefault("status", true);
            boolean tps = (Boolean) endpoints.getOrDefault("tps", true);
            boolean playersEnabled = (Boolean) endpoints.getOrDefault("players", true);

            int playersMaxList = ((Number) players.getOrDefault("max-list", 50)).intValue();

            return new Config(
                    port,
                    authKey,
                    run,
                    log,
                    ping,
                    info,
                    status,
                    tps,
                    playersEnabled,
                    playersMaxList,
                    bridgeVersion
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to load HungerBridge config", e);
        }
    }
}
