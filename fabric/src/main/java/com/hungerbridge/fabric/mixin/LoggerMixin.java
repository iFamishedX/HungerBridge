package com.hungerbridge.fabric.mixin;

import com.hungerbridge.fabric.OutputCapture;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Logger.class)
public class LoggerMixin {

    @Inject(
            method = "info(Ljava/lang/String;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void hungerbridge_captureInfo(String message, CallbackInfo ci) {
        if (OutputCapture.isActive()) {
            OutputCapture.add(message);
            ci.cancel(); // suppress console output
        }
    }
}
