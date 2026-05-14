package com.hungerbridge.paper;

import com.hungerbridge.common.BridgeServer;
import com.hungerbridge.common.Config;
import com.hungerbridge.common.util.Platform;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class PaperPlugin extends JavaPlugin {

    private BridgeServer bridgeServer;

    @Override
    public void onEnable() {
        Platform.setAdapter(new PaperPlatformAdapter(this));
        try {
            Platform.ServerAdapter adapter = Platform.adapter();
            Path cfgDir = adapter.getConfigDir(getServer());
            Config config = Config.load(cfgDir);
            Platform.CommandExecutor executor = adapter.getCommandExecutor(getServer());
            Platform.Logger logger = adapter.getLogger();

            bridgeServer = new BridgeServer(config, executor, logger);
            bridgeServer.start();

            getLogger().info("HungerBridge HTTP server started on port " + config.port);
        } catch (Exception e) {
            getLogger().severe("Failed to start HungerBridge: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (bridgeServer != null) bridgeServer.stop();
    }
}
