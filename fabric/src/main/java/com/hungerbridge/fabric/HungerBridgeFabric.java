package com.hungerbridge.fabric;

import com.hungerbridge.common.Config;
import com.hungerbridge.common.Server;
import com.hungerbridge.common.util.Platform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class HungerBridgeFabric implements ModInitializer {

    public static final String MOD_ID = "hungerbridge";
    private static final Logger LOGGER = LoggerFactory.getLogger("HungerBridge");

    private Server bridgeServer;

    @Override
    public void onInitialize() {
        LOGGER.info("HungerBridge Fabric mod initialized");

        // Register adapter BEFORE server start
        Platform.setAdapter(new FabricPlatformAdapter());

        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
    }

    private void onServerStarted(MinecraftServer server) {
        try {
            Path cfgDir = server.getRunDirectory()
                    .toPath()
                    .resolve("config")
                    .resolve("HungerBridge");

            Config config = Config.load(cfgDir);

            // Initialize Platform with adapter-provided executor + logger
            Platform.init(
                    Platform.adapter().getCommandExecutor(server),
                    Platform.adapter().getLogger()
            );

            bridgeServer = new Server(config);
            bridgeServer.start();

            LOGGER.info("HungerBridge HTTP server started on port {}", config.port);

        } catch (Exception e) {
            LOGGER.error("Failed to start HungerBridge on Fabric", e);
        }
    }
}
