package com.hungerbridge.fabric;

import com.hungerbridge.common.ServerInfoProvider;
import net.minecraft.server.MinecraftServer;

public final class FabricServerInfoProvider implements ServerInfoProvider {

    private final MinecraftServer server;

    public FabricServerInfoProvider(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public String getMinecraftVersion() {
        return server.getServerVersion(); // Mojang-mapped, correct
    }

    @Override
    public String getServerSoftware() {
        return "Fabric";
    }
}
