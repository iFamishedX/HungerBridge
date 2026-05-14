package com.hungerbridge.paper;

import com.hungerbridge.common.CommandExecutor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
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
            List<String> lines = new ArrayList<>();

            MinecraftServer nms = ((CraftServer) Bukkit.getServer()).getServer();

            // Clone the real console CommandSourceStack
            CommandSourceStack base = nms.createCommandSourceStack();

            // Wrap it to intercept output
            CommandSourceStack wrapper = base.withCallback((msg, type) -> {
                lines.add(msg.getString());
            });

            // Execute command using vanilla dispatcher
            nms.getCommands().performPrefixedCommand(wrapper, command);

            future.complete(lines);
        });

        try {
            return future.get();
        } catch (Exception e) {
            return List.of();
        }
    }
}
