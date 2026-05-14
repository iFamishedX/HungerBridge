package com.hungerbridge.common;

/**
 * Simple logging adapter used by platform modules.
 */
@FunctionalInterface
public interface Logger {
    void log(String level, String message);
}
