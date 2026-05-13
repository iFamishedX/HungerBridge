package com.hungerbridge.common.util;

import java.nio.file.Path;

public class Platform {

    public interface CommandExecutor {
        String run(String cmd, boolean silent);
    }

    public interface Logger {
        void log(String level, String msg);
    }

    public interface ServerAdapter {
        Object unwrap(Object server);
        Path getConfigDir(Object server);
        CommandExecutor getCommandExecutor(Object server);
    }

    private static ServerAdapter adapter;
    private static CommandExecutor executor;
    private static Logger logger;

    public static void setAdapter(ServerAdapter a) {
        adapter = a;
    }

    public static ServerAdapter getAdapter() {
        return adapter;
    }

    public static void init(CommandExecutor exec, Logger log) {
        executor = exec;
        logger = log;
    }

    public static CommandExecutor executor() {
        return executor;
    }

    public static Logger logger() {
        return logger;
    }

    public static void log(String level, String msg) {
        logger.log(level, msg);
    }
}
