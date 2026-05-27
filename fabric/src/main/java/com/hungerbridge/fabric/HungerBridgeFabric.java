package com.hungerbridge.fabric;

import com.hungerbridge.common.BridgeServer;
import com.hungerbridge.common.CommandExecutor;
import com.hungerbridge.common.Config;
import com.hungerbridge.common.Logger;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class HungerBridgeFabric implements DedicatedServerModInitializer {

    private static final org.slf4j.Logger SLF4J_LOGGER =
            LoggerFactory.getLogger("HungerBridge");

    private static BridgeServer bridgeServer;
    private static MinecraftServer mcServer;

    // store up to 18k samples (15 minutes at 20 TPS = 18,000 ticks).
    private static final int HB_TICK_SAMPLES = 18_000;
    private static final long[] HB_TICK_NANOS = new long[HB_TICK_SAMPLES];
    private static int HB_TICK_INDEX = 0;
    private static boolean HB_TICK_WARMED = false;
    private static long HB_TICK_COUNT = 0L; // total recorded ticks (monotonic)

    public static synchronized void recordTick(long nanos) {
        HB_TICK_NANOS[HB_TICK_INDEX] = nanos;
        HB_TICK_INDEX = (HB_TICK_INDEX + 1) % HB_TICK_SAMPLES;
        HB_TICK_COUNT++;
        if (!HB_TICK_WARMED && HB_TICK_COUNT >= HB_TICK_SAMPLES) {
            HB_TICK_WARMED = true;
        }
    }

    public static synchronized long[] getTickHistory() {
        return HB_TICK_NANOS;
    }

    public static synchronized boolean isTickHistoryWarmed() {
        return HB_TICK_WARMED;
    }

    /**
     * Compute average tick time (ms) over the last `samples` ticks.
     * If fewer samples exist, average over available samples.
     */
    public static synchronized double getAverageTickMs(int samples) {
        if (samples <= 0) return -1.0;
        long available = Math.min(HB_TICK_COUNT, HB_TICK_SAMPLES);
        if (available == 0) return -1.0;

        int toRead = (int) Math.min(samples, available);
        long sum = 0L;
        int idx = (HB_TICK_INDEX - 1 + HB_TICK_SAMPLES) % HB_TICK_SAMPLES;

        for (int i = 0; i < toRead; i++) {
            long nanos = HB_TICK_NANOS[idx];
            sum += nanos;
            idx = (idx - 1 + HB_TICK_SAMPLES) % HB_TICK_SAMPLES;
        }

        double avgNanos = (double) sum / toRead;
        return avgNanos / 1_000_000.0;
    }

    /**
     * Compute TPS for a window defined by number of samples.
     */
    public static synchronized double getTpsForSamples(int samples) {
        double avgMs = getAverageTickMs(samples);
        if (avgMs <= 0.0) return -1.0;
        return 1000.0 / avgMs;
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

        // Set platform and minecraft version
        config.setPlatform("fabric");
        config.setMinecraftVersion(server.getServerVersion());

        // Get version from fabric.mod.json (which already uses ${version} from root version.yaml)
        String modVersion = FabricLoader.getInstance()
                .getModContainer("hungerbridge")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        config.setBridgeVersion(modVersion);

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
