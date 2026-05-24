package com.hungerbridge.fabric;

import com.hungerbridge.common.ServerStatusProvider;
import net.minecraft.server.MinecraftServer;

/**
 * Fabric implementation of ServerStatusProvider using Mojang mappings (1.21.11).
 *
 * Uses:
 *  - server.getAverageTickTime() -> average tick time in ms
 *  - server.getCurrentPlayerCount() -> player count
 *
 * TPS is derived as 1000.0 / tickMs when tickMs is available.
 */
public final class FabricStatusProvider implements ServerStatusProvider {

    private final MinecraftServer server;

    public FabricStatusProvider(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Double getTps() {
        try {
            double tickMs = server.getAverageTickTime();
            if (tickMs <= 0.0) return null;
            return 1000.0 / tickMs;
        } catch (NoSuchMethodError | Throwable ignored) {
        }
        return null;
    }

    @Override
    public Double getTickTimeMs() {
        try {
            return (double) server.getAverageTickTime();
        } catch (NoSuchMethodError | Throwable ignored) {
        }
        return null;
    }

    @Override
    public int getPlayerCount() {
        try {
            // Mojang mapping: getCurrentPlayerCount()
            return server.getCurrentPlayerCount();
        } catch (NoSuchMethodError | Throwable ignored) {
        }
        return 0;
    }
}
