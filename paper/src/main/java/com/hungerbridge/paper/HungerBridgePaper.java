package com.hungerbridge.paper;

import com.hungerbridge.common.Config;
import com.hungerbridge.common.Server;
import com.hungerbridge.common.util.Platform;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class HungerBridgePaper extends JavaPlugin {

    private Server httpServer;

    @Override
    public void onEnable() {

        // ⭐ REQUIRED: register adapter BEFORE calling common code
        Platform.setAdapter(new PaperPlatformAdapter());

        try {
            Path cfgDir = getDataFolder().toPath().resolve("config");
            Config config = Config.load(cfgDir);

            Platform.init(
                    (cmd, silent) -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                        return ""; // ⭐ MUST return String
                    },
                    (level, msg) -> {
                        switch (level.toLowerCase()) {
                            case "error" -> getLogger().severe(msg);
                            case "warn", "warning" -> getLogger().warning(msg);
                            case "debug" -> getLogger().info("[DEBUG] " + msg);
                            case "trace" -> getLogger().info("[TRACE] " + msg);
                            default -> getLogger().info(msg);
                        }
                    }
            );

            httpServer = new Server(config);
            httpServer.start();
            getLogger().info("HungerBridge HTTP server started on port " + config.port);

        } catch (Exception e) {
            getLogger().severe("Failed to start HungerBridge: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (httpServer != null) {
            httpServer.stop();
        }
    }
}
