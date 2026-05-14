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

    private BridgeServer bridgeServer;

    @Override
    public void onInitializeServer() {
        SLF4J_LOGGER.info("HungerBridge (Fabric) initializing.");

        // Register lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
    }

    private void onServerStarted(MinecraftServer server) {
        SLF4J_LOGGER.info("HungerBridge (Fabric) starting...");

        Logger logger = new FabricLoggerAdapter(SLF4J_LOGGER);

        // <server>/config/HungerBridge/config.yaml
        Path configDir = server.getFile("config").toPath().resolve("HungerBridge");
        Config config = Config.load(configDir, logger);

        CommandExecutor executor = cmd ->
                server.getCommands().performPrefixedCommand(
                        server.createCommandSourceStack(), cmd
                );

        bridgeServer = new BridgeServer(config, logger, executor);
        bridgeServer.start();

        SLF4J_LOGGER.info("HungerBridge (Fabric) started on port {}", config.port);
    }

    private void onServerStopping(MinecraftServer server) {
        if (bridgeServer != null) {
            SLF4J_LOGGER.info("HungerBridge (Fabric) stopping...");
            bridgeServer.stop();
            bridgeServer = null;
        }
    }
}
