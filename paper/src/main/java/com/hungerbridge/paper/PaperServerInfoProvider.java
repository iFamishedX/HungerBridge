package com.hungerbridge.paper;

import com.hungerbridge.common.ServerStatusProvider;
import org.bukkit.Server;

/**
 * Paper implementation of ServerStatusProvider using Bukkit/Paper APIs.
 */
public final class PaperStatusProvider implements ServerStatusProvider {

    private final Server server;

    public PaperStatusProvider(Server server) {
        this.server = server;
    }

    @Override
    public Double getTps() {
        try {
            // Paper exposes getTPS() returning double[] (1m, 5m, 15m)
            double[] tps = server.getTPS();
            if (tps != null && tps.length > 0) {
                return tps[0];
            }
        } catch (NoSuchMethodError | UnsupportedOperationException ignored) {
        }
        return null;
    }

    @Override
    public Double getTickTimeMs() {
        try {
            // Paper exposes average tick time in ms
            return (double) server.getAverageTickTime();
        } catch (NoSuchMethodError | UnsupportedOperationException ignored) {
        }
        return null;
    }

    @Override
    public int getPlayerCount() {
        try {
            return server.getOnlinePlayers().size();
        } catch (NoSuchMethodError | UnsupportedOperationException ignored) {
        }
        return 0;
    }
}
