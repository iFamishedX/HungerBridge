package com.hungerbridge.paper;

import com.hungerbridge.common.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            CommandSender console = plugin.getServer().getConsoleSender();

            // Paper API: safely capture output from ANY command
            List<String> out = console.performCommandWithOutput(command);

            future.complete(out);
        });

        try {
            return future.get();
        } catch (Exception e) {
            return List.of();
        }
    }
}
