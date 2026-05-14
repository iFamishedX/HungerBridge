package com.hungerbridge.paper;

import com.hungerbridge.common.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class PaperCommandExecutor implements CommandExecutor {

    private final JavaPlugin plugin;

    public PaperCommandExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(String command) {
        plugin.getServer().getScheduler().runTask(plugin, () ->
                plugin.getServer().dispatchCommand(
                        plugin.getServer().getConsoleSender(),
                        command
                )
        );
    }

    @Override
    public List<String> executeWithOutput(String command) {
        // For now, just execute and don't capture output.
        execute(command);
        return null;
    }
}
