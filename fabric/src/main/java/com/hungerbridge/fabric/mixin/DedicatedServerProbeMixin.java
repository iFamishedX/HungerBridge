package com.hungerbridge.fabric.mixin;

import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DedicatedServer.class)
public class DedicatedServerProbeMixin {
    static {
        System.out.println("[HB/DIAG] DedicatedServer obf name = "
                + DedicatedServer.class.getName());
    }
}
