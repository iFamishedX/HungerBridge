package com.hungerbridge.fabric.mixin;

import com.hungerbridge.fabric.OutputCapture;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConsoleAppender.class)
public class ConsoleAppenderMixin {

    @Inject(
            method = "append",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void hungerbridge_captureConsole(LogEvent event, CallbackInfo ci) {
        if (OutputCapture.isActive()) {
            String msg = event.getMessage().getFormattedMessage();
            OutputCapture.add(msg);
            ci.cancel(); // suppress console output
        }
    }
}
