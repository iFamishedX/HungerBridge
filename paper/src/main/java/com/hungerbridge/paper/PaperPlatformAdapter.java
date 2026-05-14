package com.hungerbridge.paper;

import com.hungerbridge.common.util.Platform;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PaperPlatformAdapter implements Platform.ServerAdapter {

    private final Plugin plugin;

    public PaperPlatformAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Path getConfigDir(Object server) {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public Platform.CommandExecutor getCommandExecutor(Object server) {
        return (cmd) -> {
            List<String> output = new ArrayList<>();

            // Use the REAL console sender — no custom implementation needed
            CommandSender sender = Bukkit.getConsoleSender();

            try {
                Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                    boolean ok = Bukkit.dispatchCommand(sender, cmd);
                    output.add(ok ? "1" : "0");
                    return null;
                }).get();
            } catch (Exception e) {
                Bukkit.getLogger().severe("HungerBridge command execution failed: " + e.getMessage());
                return "0";
            }

            return output.isEmpty() ? "" : String.join("\n", output);
        };
    }

    @Override
    public Platform.Logger getLogger() {
        return (level, msg) -> {
            switch (level.toLowerCase()) {
                case "error" -> Bukkit.getLogger().severe(msg);
                case "warn", "warning" -> Bukkit.getLogger().warning(msg);
                case "debug" -> Bukkit.getLogger().info("[DEBUG] " + msg);
                case "trace" -> Bukkit.getLogger().info("[TRACE] " + msg);
                default -> Bukkit.getLogger().info(msg);
            }
        };
    }
}
