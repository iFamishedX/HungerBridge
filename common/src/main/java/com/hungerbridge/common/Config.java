package com.hungerbridge.common;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Configuration holder for HungerBridge.
 */
public final class Config {

    private final int port;
    private final String authKey;

    // Endpoint toggles
    private final boolean legacyRun;
    private final boolean legacyLog;

    private final boolean v1Run;
    private final boolean v1Log;
    private final boolean v1Status;
    private final boolean v1Version;

    private final boolean v2Run;
    private final boolean v2Log;
    private final boolean v2Ping;
    private final boolean v2Info;
    private final boolean v2Status;
    private final boolean v2Tps;
    private final boolean v2Players;

    // Players config
    private final int playersMaxList;

    // JSON API metadata
    private String platform = "unknown";
    private String minecraftVersion = "unknown";
    private final String bridgeVersion;

    public Config(
            int port,
            String authKey,
            boolean legacyRun,
            boolean legacyLog,
            boolean v1Run,
            boolean v1Log,
            boolean v1Status,
            boolean v1Version,
            boolean v2Run,
            boolean v2Log,
            boolean v2Ping,
            boolean v2Info,
            boolean v2Status,
            boolean v2Tps,
            boolean v2Players,
            int playersMaxList,
            String bridgeVersion
    ) {
        this.port = port;
        this.authKey = authKey;

        this.legacyRun = legacyRun;
        this.legacyLog = legacyLog;

        this.v1Run = v1Run;
        this.v1Log = v1Log;
        this.v1Status = v1Status;
        this.v1Version = v1Version;

        this.v2Run = v2Run;
        this.v2Log = v2Log;
        this.v2Ping = v2Ping;
        this.v2Info = v2Info;
        this.v2Status = v2Status;
        this.v2Tps = v2Tps;
        this.v2Players = v2Players;

        this.playersMaxList = playersMaxList;

        this.bridgeVersion = bridgeVersion;
    }

    public int getPort() { return port; }
    public String getAuthKey() { return authKey; }

    // Legacy toggles
    public boolean isLegacyRunEnabled() { return legacyRun; }
    public boolean isLegacyLogEnabled() { return legacyLog; }

    // v1 toggles
    public boolean isV1RunEnabled() { return v1Run; }
    public boolean isV1LogEnabled() { return v1Log; }
    public boolean isV1StatusEnabled() { return v1Status; }
    public boolean isV1VersionEnabled() { return v1Version; }

    // v2 toggles
    public boolean isV2RunEnabled() { return v2Run; }
    public boolean isV2LogEnabled() { return v2Log; }
    public boolean isV2PingEnabled() { return v2Ping; }
    public boolean isV2InfoEnabled() { return v2Info; }
    public boolean isV2StatusEnabled() { return v2Status; }
    public boolean isV2TpsEnabled() { return v2Tps; }
    public boolean isV2PlayersEnabled() { return v2Players; }

    public int getPlayersMaxList() { return playersMaxList; }

    public String getVersion() { return bridgeVersion; }
    public String getPlatform() { return platform; }
    public String getMinecraftVersion() { return minecraftVersion; }

    public void setPlatform(String platform) { this.platform = platform; }
    public void setMinecraftVersion(String minecraftVersion) { this.minecraftVersion = minecraftVersion; }

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

                Map<String, Object> root = new HashMap<>();
                root.put("port", 30007);

                Map<String, Object> auth = new HashMap<>();
                auth.put("key", UUID.randomUUID().toString());
                root.put("auth", auth);

                Map<String, Object> v2 = new HashMap<>();
                v2.put("run", true);
                v2.put("log", true);
                v2.put("ping", true);
                v2.put("info", true);
                v2.put("status", true);
                v2.put("tps", true);
                v2.put("players", true);
                root.put("v2-endpoints", v2);

                Map<String, Object> v1 = new HashMap<>();
                v1.put("run", false);
                v1.put("log", false);
                v1.put("status", false);
                v1.put("version", false);
                root.put("v1-endpoints", v1);

                Map<String, Object> legacy = new HashMap<>();
                legacy.put("run", false);
                legacy.put("log", false);
                root.put("legacy-endpoints", legacy);

                Map<String, Object> players = new HashMap<>();
                players.put("max-list", 50);
                root.put("players", players);

                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                options.setPrettyFlow(true);
                Yaml yaml = new Yaml(options);

                try (OutputStream out = Files.newOutputStream(configFile);
                     OutputStreamWriter writer = new OutputStreamWriter(out)) {
                    yaml.dump(root, writer);
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

            int port = ((Number) root.getOrDefault("port", 30007)).intValue();

            Map<String, Object> auth = (Map<String, Object>) root.getOrDefault("auth", new HashMap<>());
            String authKey = (String) auth.getOrDefault("key", "");

            Map<String, Object> v2 = (Map<String, Object>) root.getOrDefault("v2-endpoints", new HashMap<>());
            Map<String, Object> v1 = (Map<String, Object>) root.getOrDefault("v1-endpoints", new HashMap<>());
            Map<String, Object> legacy = (Map<String, Object>) root.getOrDefault("legacy-endpoints", new HashMap<>());
            Map<String, Object> players = (Map<String, Object>) root.getOrDefault("players", new HashMap<>());

            return new Config(
                    port,
                    authKey,

                    // legacy
                    (Boolean) legacy.getOrDefault("run", false),
                    (Boolean) legacy.getOrDefault("log", false),

                    // v1
                    (Boolean) v1.getOrDefault("run", false),
                    (Boolean) v1.getOrDefault("log", false),
                    (Boolean) v1.getOrDefault("status", false),
                    (Boolean) v1.getOrDefault("version", false),

                    // v2
                    (Boolean) v2.getOrDefault("run", true),
                    (Boolean) v2.getOrDefault("log", true),
                    (Boolean) v2.getOrDefault("ping", true),
                    (Boolean) v2.getOrDefault("info", true),
                    (Boolean) v2.getOrDefault("status", true),
                    (Boolean) v2.getOrDefault("tps", true),
                    (Boolean) v2.getOrDefault("players", true),

                    // players
                    ((Number) players.getOrDefault("max-list", 50)).intValue(),

                    bridgeVersion
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to load HungerBridge config", e);
        }
    }
}
