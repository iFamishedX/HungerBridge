package com.hungerbridge.paper;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.ArrayList;
import java.util.List;

public class PaperCommandExecutor {

    public String run(String command, boolean silent) {
        ConsoleCommandSender console = Bukkit.getConsoleSender();

        if (silent) {
            Bukkit.dispatchCommand(console, command);
            return "";
        }

        CapturingSender sender = new CapturingSender(console);
        Bukkit.dispatchCommand(sender, command);
        return String.join("\n", sender.getLines());
    }

    private static class CapturingSender extends org.bukkit.command.CommandSenderWrapper {

        private final List<String> lines = new ArrayList<>();

        public CapturingSender(CommandSender wrapped) {
            super(wrapped);
        }

        @Override
        public void sendMessage(String message) {
            lines.add(message);
        }

        public List<String> getLines() {
            return lines;
        }
    }
}
