package com.hungerbridge.common;

import java.util.List;

/**
 * Interface for executing server commands from the HTTP bridge.
 * Implemented by platform-specific modules.
 */
public interface CommandExecutor {

    /**
     * Execute a command string on the underlying server.
     */
    void execute(String command);

    /**
     * Execute a command and return output lines.
     * Fabric cannot capture output, so it returns null.
     */
    default List<String> executeWithOutput(String command) {
        execute(command);
        return null;
    }
}
