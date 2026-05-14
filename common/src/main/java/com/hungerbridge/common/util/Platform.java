package com.hungerbridge.common.util;

import java.nio.file.Path;

public class Platform {

    // Stored adapter instance
    private static ServerAdapter adapter;

    // Stored executor + logger
    private static CommandExecutor executor;
    private static Logger logger;

    // -----------------------------
    // Interfaces
    // -----------------------------

    public interface CommandExecutor {
        String run(String command, boolean silent);
    }

    public interface Logger {
        void log(String level, String message);
    }

    public interface ServerAdapter {
        Path getConfigDir(Object server);

        CommandExecutor getCommandExecutor(Object server);

        Logger getLogger();
    }

    // -----------------------------
    // Adapter registration
    // -----------------------------

    public static void setAdapter(ServerAdapter a) {
        adapter = a;
    }

    public static ServerAdapter adapter() {
        return adapter;
    }

    // -----------------------------
    // Initialization
    // -----------------------------

    public static void init(CommandExecutor exec, Logger log) {
        executor = exec;
        logger = log;
    }

    // -----------------------------
    // Accessors
    // -----------------------------

    public static CommandExecutor executor() {
        return executor;
    }

    public static Logger logger() {
        return logger;
    }
}
