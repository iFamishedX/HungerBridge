package com.hungerbridge.fabric;

import com.hungerbridge.common.BridgeServer;
import com.hungerbridge.common.Config;
import com.hungerbridge.common.util.Platform;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class HungerBridgeFabric implements DedicatedServerModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("HungerBridge");
    private static BridgeServer bridgeServer;

    @Override
    public void onInitializeServer() {
        LOGGER.info("HungerBridge Fabric initialized");
        Platform.setAdapter(new FabricPlatformAdapter());
    }

    // Call this from a mixin or Fabric lifecycle event when you have the server instance.
    public static void startForServer(MinecraftServer server) {
        try {
            Platform.ServerAdapter adapter = Platform.adapter();
            Path cfgDir = adapter.getConfigDir(server);
            Config config = Config.load(cfgDir);
            Platform.CommandExecutor executor = adapter.getCommandExecutor(server);
            Platform.Logger logger = adapter.getLogger();

            bridgeServer = new BridgeServer(config, executor, logger);
            bridgeServer.start();

            logger.log("info", "HungerBridge HTTP server started on port " + config.port);
        } catch (Exception e) {
            LOGGER.error("Failed to start HungerBridge on Fabric", e);
        }
    }

    public static void stop() {
        if (bridgeServer != null) bridgeServer.stop();
    }
}
