package com.hungerbridge.paper;

import org.bukkit.Bukkit;

public class PaperCommandExecutor {

    public void run(String cmd, boolean silent) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }
}
