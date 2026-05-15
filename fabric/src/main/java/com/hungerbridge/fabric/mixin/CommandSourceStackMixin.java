package com.hungerbridge.fabric.mixin;

import com.hungerbridge.fabric.OutputCapture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSourceStack.class)
public class CommandSourceStackMixin {

    static {
        System.out.println("[HungerBridge/DIAG] CommandSourceStackMixin LOADED (class loaded)");
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void diagInit(CallbackInfo ci) {
        System.out.println("[HungerBridge/DIAG] CommandSourceStackMixin APPLIED (constructor patched)");
    }

    @Inject(method = "sendSystemMessage", at = @At("HEAD"))
    private void diagSend(Component message, CallbackInfo ci) {
        System.out.println("[HungerBridge/DIAG] sendSystemMessage FIRED: " + message.getString());
    }

    @Inject(method = "sendSystemMessage", at = @At("HEAD"), cancellable = true)
    private void hungerbridge_captureOutput(Component message, CallbackInfo ci) {
        if (OutputCapture.isActive()) {
            OutputCapture.add(message.getString());
            ci.cancel();
        }
    }
}
