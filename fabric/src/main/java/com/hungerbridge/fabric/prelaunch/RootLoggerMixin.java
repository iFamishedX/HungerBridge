package com.hungerbridge.fabric.prelaunch;

import com.hungerbridge.fabric.OutputCapture;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Logger.class, remap = false)
public class RootLoggerMixin {

    @Inject(method = "callAppenders", at = @At("HEAD"), cancellable = true)
    private void hungerbridge_capture(LogEvent event, CallbackInfo ci) {
        if (OutputCapture.isActive()) {
            OutputCapture.add(event.getMessage().getFormattedMessage());
            ci.cancel(); // suppress console output
        }
    }
}
