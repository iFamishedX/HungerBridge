package com.hungerbridge.paper;

import com.hungerbridge.common.ServerInfoProvider;
import org.bukkit.Server;

public final class PaperServerInfoProvider implements ServerInfoProvider {

    private final Server server;

    public PaperServerInfoProvider(Server server) {
        this.server = server;
    }

    @Override
    public String getMinecraftVersion() {
        return server.getMinecraftVersion();
    }

    @Override
    public String getServerSoftware() {
        return server.getName();
    }
}
