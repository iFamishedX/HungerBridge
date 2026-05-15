package com.hungerbridge.fabric.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public class MinecraftServerNameProbeMixin {
    static {
        System.out.println("[HungerBridge/DIAG] MinecraftServer maps to: "
                + MinecraftServer.class.getName());
    }
}
