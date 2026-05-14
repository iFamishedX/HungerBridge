package com.hungerbridge.fabric;

import com.hungerbridge.common.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter that bridges the common Logger interface to SLF4J.
 */
public final class FabricLoggerAdapter implements Logger {

    private final org.slf4j.Logger logger;

    public FabricLoggerAdapter(String name) {
        this.logger = LoggerFactory.getLogger(name);
    }

    public FabricLoggerAdapter(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(String level, String message) {
        switch (level.toUpperCase()) {
            case "WARN":
                logger.warn(message);
                break;
            case "ERROR":
                logger.error(message);
                break;
            case "DEBUG":
                logger.debug(message);
                break;
            default:
                logger.info(message);
                break;
        }
    }
}
