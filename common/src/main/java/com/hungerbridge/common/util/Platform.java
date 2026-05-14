package com.hungerbridge.common.util;

import java.nio.file.Path;

public class Platform {

    private static ServerAdapter adapter;

    private static CommandExecutor executor;
    private static Logger logger;

    public interface CommandExecutor {
        String run(String command);
    }

    public interface Logger {
        void log(String level, String message);
    }

    public interface ServerAdapter {
        Path getConfigDir(Object server);

        CommandExecutor getCommandExecutor(Object server);

        Logger getLogger();
    }

    public static void setAdapter(ServerAdapter a) {
        adapter = a;
    }

    public static ServerAdapter adapter() {
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
}
