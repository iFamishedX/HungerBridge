package com.hungerbridge.fabric;

import com.hungerbridge.common.ServerStatusProvider;
import net.minecraft.server.MinecraftServer;

/**
 * Fabric implementation of ServerStatusProvider using Mojang mappings (1.21.11).
 *
 * Uses:
 *  - server.getAverageNanosPerTick() -> average tick time in nanoseconds
 *  - server.getPlayerManager().getPlayerList().size() -> player count
 *
 * TPS is derived as 1_000_000_000.0 / nanosPerTick.
 */
public final class FabricStatusProvider implements ServerStatusProvider {

    private final MinecraftServer server;

    public FabricStatusProvider(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Double getTps() {
        try {
            long nanos = server.getAverageNanosPerTick();
            if (nanos <= 0L) return null;
            return 1_000_000_000.0 / nanos;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    public Double getTickTimeMs() {
        try {
            long nanos = server.getAverageNanosPerTick();
            if (nanos <= 0L) return null;
            return nanos / 1_000_000.0;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    public int getPlayerCount() {
        try {
            return server.getPlayerManager().getPlayerList().size();
        } catch (Throwable ignored) {
            return 0;
        }
    }
}
