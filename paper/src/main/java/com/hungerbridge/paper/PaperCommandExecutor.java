package com.hungerbridge.paper;

import com.hungerbridge.common.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Proxy;
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

            CommandSender console = plugin.getServer().getConsoleSender();

            CommandSender proxy = (CommandSender) Proxy.newProxyInstance(
                    console.getClass().getClassLoader(),
                    new Class[]{CommandSender.class},
                    (obj, method, args) -> {
                        if (method.getName().equals("sendMessage")) {
                            if (args != null) {
                                for (Object arg : args) {
                                    if (arg instanceof String s) {
                                        lines.add(s);
                                    }
                                }
                            }
                            return null;
                        }
                        return method.invoke(console, args);
                    }
            );

            plugin.getServer().dispatchCommand(proxy, command);
            future.complete(lines);
        });

        try {
            return future.get();
        } catch (Exception e) {
            return List.of();
        }
    }
}
