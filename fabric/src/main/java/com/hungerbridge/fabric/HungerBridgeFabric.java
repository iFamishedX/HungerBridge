package com.hungerbridge.fabric;

import com.hungerbridge.common.BridgeServer;
import com.hungerbridge.common.CommandExecutor;
import com.hungerbridge.common.Config;
import com.hungerbridge.common.Logger;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class HungerBridgeFabric implements DedicatedServerModInitializer {

    private static final org.slf4j.Logger SLF4J_LOGGER =
            LoggerFactory.getLogger("HungerBridge");

    private static BridgeServer bridgeServer;

    @Override
    public void onInitializeServer() {
        SLF4J_LOGGER.info("HungerBridge (Fabric) initializing.");

        // Start when the dedicated server is fully started
        ServerLifecycleEvents.SERVER_STARTED.register(HungerBridgeFabric::onServerStarted);

        // Stop cleanly on shutdown
        ServerLifecycleEvents.SERVER_STOPPING.register(HungerBridgeFabric::onServerStopping);
    }

    private static void onServerStarted(MinecraftServer server) {
        SLF4J_LOGGER.info("HungerBridge (Fabric) starting...");

        Logger logger = new FabricLoggerAdapter(SLF4J_LOGGER);

        Path configDir = server.getFile("config").toPath().resolve("HungerBridge");
        Config config = Config.load(configDir, logger);

        config.setPlatform("fabric");
        config.setMinecraftVersion(server.getVersion());

        CommandExecutor executor = new FabricCommandExecutor(server);

        bridgeServer = new BridgeServer(config, logger, executor);
        bridgeServer.start();

        SLF4J_LOGGER.info("HungerBridge (Fabric) started on port {}", config.getPort());
    }

    private static void onServerStopping(MinecraftServer server) {
        if (bridgeServer != null) {
            SLF4J_LOGGER.info("HungerBridge (Fabric) stopping...");
            bridgeServer.stop();
            bridgeServer = null;
        }
    }
}
