package com.hungerbridge.common;

/**
 * Abstraction for platform modules to provide server status metrics
 * (TPS, tick time, player count) to the common HTTP bridge.
 */
public interface ServerStatusProvider {

    /**
     * Current TPS (e.g. last 1s or 1m average depending on platform).
     * Return null if not available.
     */
    Double getTps();

    /**
     * Average tick time in milliseconds.
     * Return null if not available.
     */
    Double getTickTimeMs();

    /**
     * Current online player count.
     * Return 0 if not available.
     */
    int getPlayerCount();
}
