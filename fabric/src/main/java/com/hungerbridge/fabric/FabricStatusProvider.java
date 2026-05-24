package com.hungerbridge.fabric;

import com.hungerbridge.common.ServerStatusProvider;
import net.minecraft.server.MinecraftServer;

public final class FabricStatusProvider implements ServerStatusProvider {

    private final MinecraftServer server;

    public FabricStatusProvider(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Double getTps() {
        return null;
    }

    @Override
    public Double getTickTimeMs() {
        return null;
    }

    @Override
    public int getPlayerCount() {
        return server.getPlayerManager().getPlayerList().size();
    }
}
