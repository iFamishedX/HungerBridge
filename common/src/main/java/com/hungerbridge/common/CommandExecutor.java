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
     *
     * @param command     Command to execute.
     * @param showConsole If true, platform should NOT suppress console output
     *                    for this execution. If false, platform may temporarily
     *                    remove appenders to keep the console clean.
     * @return List of output lines, or null if the platform cannot capture output.
     */
    List<String> executeWithOutput(String command, boolean showConsole);

    /**
     * Execute a command and return output lines, in "silent" mode:
     * platforms are allowed to suppress console output.
     */
    default List<String> executeWithOutput(String command) {
        return executeWithOutput(command, false);
    }
}
