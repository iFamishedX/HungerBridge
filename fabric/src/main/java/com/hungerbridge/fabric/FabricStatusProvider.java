package com.hungerbridge.fabric;

import com.hungerbridge.common.ServerStatusProvider;
import net.minecraft.server.MinecraftServer;

/**
 * Fabric implementation of ServerStatusProvider using Mojang mappings (1.21.11).
 *
 * Uses:
 *  - server.getAverageTickTime() -> average tick time in milliseconds
 *  - server.getCurrentPlayerCount() -> player count
 *
 * TPS is derived as 1000.0 / tickMs.
 */
public final class FabricStatusProvider implements ServerStatusProvider {

    private final MinecraftServer server;

    public FabricStatusProvider(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Double getTps() {
        double tickMs = server.getAverageTickTime();
        if (tickMs <= 0.0) {
            return null;
        }
        return 1000.0 / tickMs;
    }

    @Override
    public Double getTickTimeMs() {
        double tickMs = server.getAverageTickTime();
        if (tickMs <= 0.0) {
            return null;
        }
        return tickMs;
    }

    @Override
    public int getPlayerCount() {
        return server.getCurrentPlayerCount();
    }
}
