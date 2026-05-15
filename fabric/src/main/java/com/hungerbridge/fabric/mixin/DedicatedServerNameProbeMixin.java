package com.hungerbridge.fabric.mixin;

import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DedicatedServer.class)
public class DedicatedServerNameProbeMixin {
    static {
        System.out.println("[HungerBridge/DIAG] DedicatedServer maps to: "
                + DedicatedServer.class.getName());
    }
}
