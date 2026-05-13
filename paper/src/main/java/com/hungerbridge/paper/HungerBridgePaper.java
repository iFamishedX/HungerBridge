package com.hungerbridge.paper;

import com.hungerbridge.common.Config;
import com.hungerbridge.common.Server;
import com.hungerbridge.common.util.Platform;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.logging.Logger;

public class HungerBridgePaper extends JavaPlugin {

    private Server server;

    @Override
    public void onEnable() {
        Logger log = getLogger();
        try {
            Path cfgDir = getDataFolder().toPath();
            Config config = Config.load(cfgDir);

            PaperCommandExecutor exec = new PaperCommandExecutor();

            Platform.init(
                    (cmd, silent) -> exec.run(cmd, silent),
                    (level, msg) -> {
                        switch (level.toLowerCase()) {
                            case "error" -> log.severe(msg);
                            case "warn", "warning" -> log.warning(msg);
                            case "debug", "trace" -> log.fine(msg);
                            default -> log.info(msg);
                        }
                    }
            );

            server = new Server(config);
            server.start();
            log.info("HungerBridge HTTP server started on port " + config.port);
        } catch (Exception e) {
            log.severe("Failed to start HungerBridge: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (server != null) {
            server.stop();
        }
    }
}
