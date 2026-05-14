package com.hungerbridge.fabric;

import com.hungerbridge.common.Config;
import com.hungerbridge.common.Server;
import com.hungerbridge.common.util.Platform;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class HungerBridgeFabric implements DedicatedServerModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("HungerBridge");
    private Server bridgeServer;

    @Override
    public void onInitializeServer() {
        LOGGER.info("HungerBridge Fabric mod initialized");

        Platform.setAdapter(new FabricPlatformAdapter());
    }

    @Override
    public void onServerStarting(MinecraftServer server) {
        try {
            Path cfgDir = server.getRunDirectory()
                    .toPath()
                    .resolve("config")
                    .resolve("HungerBridge");

            Config config = Config.load(cfgDir);

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
