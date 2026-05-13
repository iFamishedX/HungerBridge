package com.hungerbridge.fabric.mixin;

import com.hungerbridge.common.HungerBridgeCommon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {

    @Inject(method = "initServer", at = @At("RETURN"))
    private void hungerbridge$onServerStart(CallbackInfoReturnable<Boolean> cir) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        HungerBridgeCommon.onServerStart(server);
    }
}
