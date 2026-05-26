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
    private static MinecraftServer mcServer;

    private static final int HB_TICK_SAMPLES = 100;
    private static final long[] HB_TICK_NANOS = new long[HB_TICK_SAMPLES];
    private static int HB_TICK_INDEX = 0;
    private static boolean HB_TICK_WARMED = false;

    public static void recordTick(long nanos) {
        HB_TICK_NANOS[HB_TICK_INDEX] = nanos;
        HB_TICK_INDEX = (HB_TICK_INDEX + 1) % HB_TICK_SAMPLES;

        if (!HB_TICK_WARMED && HB_TICK_INDEX == 0) {
            HB_TICK_WARMED = true;
        }
    }

    public static long[] getTickHistory() {
        return HB_TICK_NANOS;
    }

    public static boolean isTickHistoryWarmed() {
        return HB_TICK_WARMED;
    }

    @Override
    public void onInitializeServer() {
        SLF4J_LOGGER.info("HungerBridge initializing.");
    }

    // Called by mixin on first server tick
    public static void onServerStarted(MinecraftServer server) {
        SLF4J_LOGGER.info("HungerBridge starting...");

        mcServer = server;

        Logger logger = new FabricLoggerAdapter(SLF4J_LOGGER);

        Path configDir = server.getFile("config").resolve("HungerBridge");
        Config config = Config.load(configDir, logger);

        config.setPlatform("fabric");
        config.setMinecraftVersion(server.getServerVersion());

        CommandExecutor executor = new FabricCommandExecutor(server);

        // Info provider exists but is NOT passed into BridgeServer
        FabricServerInfoProvider infoProvider = new FabricServerInfoProvider(server);

        bridgeServer = new BridgeServer(config, logger, executor);
        bridgeServer.start();

        SLF4J_LOGGER.info("HungerBridge started on port {}", config.getPort());
    }

    // Called by mixin on server shutdown
    public static void onServerStopping() {
        if (bridgeServer != null) {
            SLF4J_LOGGER.info("HungerBridge stopping...");
            bridgeServer.stop();
            bridgeServer = null;
        }

        mcServer = null;
    }

    public static MinecraftServer getServer() {
        return mcServer;
    }
}
