package com.hungerbridge.fabric;

import com.hungerbridge.common.Config;
import com.hungerbridge.common.Platform;
import com.hungerbridge.common.Server;
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

        // Run AFTER the server has fully started
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
    }

    private void onServerStarted(MinecraftServer server) {
        try {
            // /config/HungerBridge/
            Path cfgDir = server.getRunDirectory()
                    .toPath()
                    .resolve("config")
                    .resolve("HungerBridge");

            // Load config (creates config.yaml if missing)
            Config config = Config.load(cfgDir);

            // Fabric command executor wrapper
            FabricCommandExecutor exec = new FabricCommandExecutor(server);

            // Initialize platform bridge
            Platform.init(
                    (cmd, silent) -> exec.run(cmd)
                    (level, msg) -> LOGGER.info(msg)
            );

            // Start HTTP server
            bridgeServer = new Server(config);
            bridgeServer.start();

            LOGGER.info("HungerBridge HTTP server started on port {}", config.port);

        } catch (Exception e) {
            LOGGER.error("Failed to start HungerBridge on Fabric", e);
        }
    }
}
