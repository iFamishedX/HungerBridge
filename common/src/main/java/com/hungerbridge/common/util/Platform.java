package com.hungerbridge.common.util;

public class Platform {

    public interface CommandRunner {
        String run(String command, boolean silent) throws Exception;
    }

    public interface Logger {
        void log(String level, String message);
    }

    private static CommandRunner commandRunner;
    private static Logger logger;

    public static void init(CommandRunner cr, Logger lg) {
        commandRunner = cr;
        logger = lg;
    }

    public static CommandRunner commandRunner() {
        if (commandRunner == null) throw new IllegalStateException("CommandRunner not initialized");
        return commandRunner;
    }

    public static Logger logger() {
        if (logger == null) throw new IllegalStateException("Logger not initialized");
        return logger;
    }
}
