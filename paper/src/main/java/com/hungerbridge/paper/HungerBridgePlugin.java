package com.hungerbridge.paper;

import com.hungerbridge.common.BridgeServer;
import com.hungerbridge.common.Config;
import com.hungerbridge.common.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class HungerBridgePlugin extends JavaPlugin {

    private BridgeServer bridgeServer;

    @Override
    public void onEnable() {
        getLogger().info("HungerBridge enabling...");

        Logger loggerAdapter = (level, message) -> {
            switch (level.toLowerCase()) {
                case "error" -> getLogger().severe(message);
                case "warn", "warning" -> getLogger().warning(message);
                case "debug" -> getLogger().info("[DEBUG] " + message);
                default -> getLogger().info(message);
            }
        };

        try {
            Path cfgDir = getDataFolder().toPath();
            Config config = Config.load(cfgDir);

            bridgeServer = new BridgeServer(config, loggerAdapter);
            bridgeServer.start();

            getLogger().info("HungerBridge enabled. HTTP /log on port " + config.port);
        } catch (Exception e) {
            getLogger().severe("Failed to start HungerBridge: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (bridgeServer != null) {
            bridgeServer.stop();
            bridgeServer = null;
        }
        getLogger().info("HungerBridge disabled.");
    }
}
