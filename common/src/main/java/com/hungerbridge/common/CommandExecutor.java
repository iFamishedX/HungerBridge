package com.hungerbridge.common;

/**
 * Functional interface for executing server commands from the HTTP bridge.
 * Implemented by platform-specific modules.
 */
@FunctionalInterface
public interface CommandExecutor {

    /**
     * Execute a command string on the underlying server.
     *
     * @param command command to execute (without leading slash)
     */
    void execute(String command);
}
