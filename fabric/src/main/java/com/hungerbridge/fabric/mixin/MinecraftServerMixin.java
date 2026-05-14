package com.hungerbridge.fabric.mixin;

import com.hungerbridge.fabric.HungerBridgeFabric;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Unique
    private boolean hungerbridge$started = false;

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void hungerbridge$onFirstTick(CallbackInfo ci) {
        if (!hungerbridge$started) {
            hungerbridge$started = true;
            HungerBridgeFabric.onServerStarted((MinecraftServer) (Object) this);
        }
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void hungerbridge$onStop(CallbackInfo ci) {
        HungerBridgeFabric.onServerStopping();
    }
}
