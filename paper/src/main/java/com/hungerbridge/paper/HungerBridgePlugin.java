package com.hungerbridge.paper;

import com.hungerbridge.common.BridgeServer;
import com.hungerbridge.common.CommandExecutor;
import com.hungerbridge.common.Config;
import com.hungerbridge.common.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

/**
 * Paper implementation of HungerBridge.
 * Starts the HTTP bridge on plugin enable and stops it on disable.
 */
public final class HungerBridgePlugin extends JavaPlugin {

    private BridgeServer bridgeServer;

    @Override
    public void onEnable() {
        Logger logger = (level, message) -> {
            switch (level.toUpperCase()) {
                case "WARN":
                    getLogger().warning(message);
                    break;
                case "ERROR":
                    getLogger().severe(message);
                    break;
                default:
                    getLogger().info(message);
                    break;
            }
        };

        Path configDir = getDataFolder().toPath();
        Config config = Config.load(configDir, logger);

        // Ensure commands are always executed on the main server thread
        CommandExecutor executor = command ->
                getServer().getScheduler().runTask(this, () ->
                        getServer().dispatchCommand(getServer().getConsoleSender(), command)
                );

        bridgeServer = new BridgeServer(config, logger, executor);
        bridgeServer.start();

        getLogger().info("HungerBridge (Paper) enabled.");
    }

    @Override
    public void onDisable() {
        if (bridgeServer != null) {
            bridgeServer.stop();
            bridgeServer = null;
        }
        getLogger().info("HungerBridge (Paper) disabled.");
    }
}
