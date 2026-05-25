package com.hungerbridge.common;

public interface ServerStatusProvider {
    double getTps();
    double getTps1m();
    double getTps5m();
    double getTps15m();
    double getTickTimeMs();
}
