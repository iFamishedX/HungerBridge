package com.hungerbridge.fabric;

import com.hungerbridge.common.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple adapter that forwards to SLF4J.
 */
public class FabricLoggerAdapter implements Logger {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger("HungerBridge");

    @Override
    public void log(String level, String message) {
        switch (level.toLowerCase()) {
            case "error" -> logger.error(message);
            case "warn", "warning" -> logger.warn(message);
            case "debug" -> logger.debug(message);
            default -> logger.info(message);
        }
    }
}
