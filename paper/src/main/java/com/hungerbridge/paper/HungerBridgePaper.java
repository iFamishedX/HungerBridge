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

        // Register Paper adapter
        Platform.setAdapter(new PaperPlatformAdapter(this));

        try {
            Path cfgDir = getDataFolder().toPath().resolve("config");
            Config config = Config.load(cfgDir);

            // Initialize Platform with adapter-provided executor + logger
            Platform.init(
                    Platform.adapter().getCommandExecutor(null),
                    Platform.adapter().getLogger()
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
