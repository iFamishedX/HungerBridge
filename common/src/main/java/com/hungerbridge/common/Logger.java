package com.hungerbridge.common;

/**
 * Simple functional logging interface used by the common module.
 * Implementations are provided by platform-specific modules.
 */
@FunctionalInterface
public interface Logger {

    /**
     * Log a message at the given level.
     *
     * @param level   log level (e.g. "INFO", "WARN", "ERROR")
     * @param message message to log
     */
    void log(String level, String message);
}
