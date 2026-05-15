package com.hungerbridge.fabric.mixin;

import com.hungerbridge.fabric.OutputCapture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin {

  @Inject(method = "sendSystemMessage", at = @At("HEAD"), cancellable = true)
  private void hb$sendSystemMessage(Component message, boolean allowLogging, CallbackInfo ci) {
    if (OutputCapture.isActive()) {
      OutputCapture.add(message.getString());
      ci.cancel();
    }
  }

  @Inject(method = "sendSuccess", at = @At("HEAD"), cancellable = true)
  private void hb$sendSuccess(Supplier<Component> messageSupplier, boolean broadcastToOps, CallbackInfo ci) {
    if (OutputCapture.isActive()) {
      OutputCapture.add(messageSupplier.get().getString());
      ci.cancel();
    }
  }

  @Inject(method = "sendFailure", at = @At("HEAD"), cancellable = true)
  private void hb$sendFailure(Supplier<Component> messageSupplier, CallbackInfo ci) {
    if (OutputCapture.isActive()) {
      OutputCapture.add(messageSupplier.get().getString());
      ci.cancel();
    }
  }
}
