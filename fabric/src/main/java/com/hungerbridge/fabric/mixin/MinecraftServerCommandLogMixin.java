package com.hungerbridge.fabric.mixin;

import com.hungerbridge.fabric.OutputCapture;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerCommandLogMixin {

    static {
        System.out.println("[HB/DIAG] MinecraftServerCommandLogMixin LOADED");
    }

    @Inject(
            method = "method_43496", 
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void hungerbridge_captureCommandOutput(Object message, CallbackInfo ci) {
        Component c = (Component) message;
        System.out.println("[HB/DIAG] method_43496 FIRED: " + c.getString());

        if (OutputCapture.isActive()) {
            OutputCapture.add(c.getString());
            ci.cancel();
        }
    }
}
