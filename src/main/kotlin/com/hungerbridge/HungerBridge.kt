package com.hungerbridge

import org.bukkit.plugin.java.JavaPlugin

class HungerBridge : JavaPlugin() {

    override fun onEnable() {
        logger.info("HungerBridge has started.")
    }

    override fun onDisable() {
        logger.info("HungerBridge has stopped.")
    }
}
