package com.hungerbridge.common.util;

import java.nio.file.Path;

public final class Platform {

    private Platform() {}

    public interface ServerAdapter {
        Path getConfigDir(Object server);
        CommandExecutor getCommandExecutor(Object server);
        Logger getLogger();
    }

    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Execute a command on the server and return its textual output.
         * Implementations should return exactly what the server would send
         * to the console / sender, joined by newlines.
         */
        String execute(String command);
    }

    @FunctionalInterface
    public interface Logger {
        void log(String level, String message);
    }

    private static ServerAdapter adapter;

    public static void setAdapter(ServerAdapter a) {
        adapter = a;
    }

    public static ServerAdapter adapter() {
        return adapter;
    }
}
