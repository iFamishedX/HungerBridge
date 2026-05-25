package com.hungerbridge.paper;

import com.hungerbridge.common.ServerStatusProvider;
import org.bukkit.Bukkit;

public class PaperStatusProvider implements ServerStatusProvider {

    @Override
    public double getTps() {
        return Bukkit.getServer().getTPS()[0];
    }

    @Override
    public double getTps1m() {
        return Bukkit.getServer().getTPS()[1];
    }

    @Override
    public double getTps5m() {
        return Bukkit.getServer().getTPS()[2];
    }

    @Override
    public double getTps15m() {
        return Bukkit.getServer().getTPS()[3];
    }

    @Override
    public double getTickTimeMs() {
        long[] times = Bukkit.getServer().getTickTimes();
        long avg = 0;
        for (long t : times) avg += t;
        avg /= times.length;
        return avg / 1_000_000.0;
    }
}
