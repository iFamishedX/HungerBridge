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
        // No output capture: just return "1" or "0" or empty.
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
