package com.hungerbridge.fabric;

import com.hungerbridge.common.BridgeServer;
import com.hungerbridge.common.Config;
import com.hungerbridge.common.Logger;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.server.MinecraftServer;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class HungerBridgeFabric implements DedicatedServerModInitializer {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("HungerBridge");
    private BridgeServer bridgeServer;

    @Override
    public void onInitializeServer() {
        LOG.info("HungerBridge (Fabric) initialized. Waiting for server instance to start.");
        // Fabric's DedicatedServerModInitializer doesn't provide the server instance directly.
        // We'll rely on server lifecycle hooks or a small delayed task in a real implementation.
        // For now this class exists and will be completed when adding server startup hook.
    }

    // Helper to be called when the MinecraftServer instance is available (to be implemented later)
    public void startForServer(MinecraftServer server) {
        Logger loggerAdapter = new FabricLoggerAdapter();
        try {
            Path cfgDir = server.getServerDirectory().toPath().resolve("config").resolve("HungerBridge");
            Config config = Config.load(cfgDir);

            bridgeServer = new BridgeServer(config, loggerAdapter);
            bridgeServer.start();

            loggerAdapter.log("info", "HungerBridge HTTP /log on port " + config.port);
        } catch (Exception e) {
            LOG.error("Failed to start HungerBridge on Fabric", e);
        }
    }

    public void stop() {
        if (bridgeServer != null) {
            bridgeServer.stop();
            bridgeServer = null;
        }
    }
}
