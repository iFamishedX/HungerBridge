package com.hungerbridge.common.util;

import java.nio.file.Path;

public class Platform {

    public interface CommandExecutor {
        void run(String cmd, boolean silent);
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

    public static ServerAdapter getServerAdapter() {
        return adapter;
    }

    public static void init(CommandExecutor exec, Logger log) {
        executor = exec;
        logger = log;
    }

    public static void log(String level, String msg) {
        logger.log(level, msg);
    }

    public interface Logger {
        void log(String level, String msg);
    }
}
