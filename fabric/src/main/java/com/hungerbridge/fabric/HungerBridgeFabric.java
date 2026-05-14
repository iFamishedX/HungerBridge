package com.hungerbridge.fabric;

import com.hungerbridge.common.BridgeServer;
import com.hungerbridge.common.CommandExecutor;
import com.hungerbridge.common.Config;
import com.hungerbridge.common.Logger;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.server.MinecraftServer;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Fabric implementation of HungerBridge.
 * Runs on dedicated servers only.
 */
public final class HungerBridgeFabric implements DedicatedServerModInitializer {

    private static final org.slf4j.Logger SLF4J_LOGGER =
            LoggerFactory.getLogger("HungerBridge");

    private BridgeServer bridgeServer;

    @Override
    public void onInitializeServer() {
        // This entrypoint is called when the dedicated server is starting.
        SLF4J_LOGGER.info("HungerBridge (Fabric) initializing.");
    }

    /**
     * Called from a server lifecycle hook to actually start the bridge.
     * In a real setup, you would wire this via a server-start callback.
     */
    public void start(MinecraftServer server) {
        Logger logger = new FabricLoggerAdapter(SLF4J_LOGGER);

        // <server>/config/HungerBridge/config.yaml
        Path configDir = server.getFile("config").resolve("HungerBridge");
        Config config = Config.load(configDir, logger);

        CommandExecutor executor = cmd ->
                server.getCommands().performPrefixedCommand(
                        server.createCommandSourceStack(), cmd
                );

        bridgeServer = new BridgeServer(config, logger, executor);
        bridgeServer.start();

        SLF4J_LOGGER.info("HungerBridge (Fabric) started.");
    }

    public void stop() {
        if (bridgeServer != null) {
            bridgeServer.stop();
            bridgeServer = null;
        }
        SLF4J_LOGGER.info("HungerBridge (Fabric) stopped.");
    }
}
