package com.hungerbridge.fabric;

import com.hungerbridge.common.BridgeServer;
import com.hungerbridge.common.CommandExecutor;
import com.hungerbridge.common.Config;
import com.hungerbridge.common.Logger;
import net.fabricmc.api.DedicatedServerModInitializer;
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
    }

    // Called by mixin on first server tick
    public static void onServerStarted(MinecraftServer server) {
        SLF4J_LOGGER.info("HungerBridge (Fabric) starting...");

        Logger logger = new FabricLoggerAdapter(SLF4J_LOGGER);

        // getFile(String) already returns a Path in Mojang mappings
        Path configDir = server.getFile("config").resolve("HungerBridge");
        Config config = Config.load(configDir, logger);

        config.setPlatform("fabric");
        // Mojang-mapped equivalent of Paper's getVersion()
        config.setMinecraftVersion(server.getServerVersion());

        CommandExecutor executor = new FabricCommandExecutor(server);

        bridgeServer = new BridgeServer(config, logger, executor);
        bridgeServer.start();

        SLF4J_LOGGER.info("HungerBridge (Fabric) started on port {}", config.getPort());
    }

    // Called by mixin on server shutdown
    public static void onServerStopping() {
        if (bridgeServer != null) {
            SLF4J_LOGGER.info("HungerBridge (Fabric) stopping...");
            bridgeServer.stop();
            bridgeServer = null;
        }
    }
}
