package com.hungerbridge.fabric;

import com.hungerbridge.common.ServerStatusProvider;
import com.hungerbridge.fabric.mixin.MinecraftServerMixin;
import net.minecraft.server.MinecraftServer;

public class FabricStatusProvider implements ServerStatusProvider {

    private final MinecraftServer server;

    public FabricStatusProvider(MinecraftServer server) {
        this.server = server;
    }

    private double calcTps(long[] times) {
        long avg = 0;
        for (long t : times) avg += t;
        avg /= times.length;
        double ms = avg / 1_000_000.0;
        return Math.min(20.0, 1000.0 / ms);
    }

    @Override
    public double getTps() {
        return calcTps(((MinecraftServerMixin) server).getTickTimes());
    }

    @Override
    public double getTps1m() { return getTps(); }
    @Override
    public double getTps5m() { return getTps(); }
    @Override
    public double getTps15m() { return getTps(); }

    @Override
    public double getTickTimeMs() {
        long[] times = ((MinecraftServerMixin) server).getTickTimes();
        long avg = 0;
        for (long t : times) avg += t;
        avg /= times.length;
        return avg / 1_000_000.0;
    }
}
