package com.hungerbridge.fabric.mixin;

import com.hungerbridge.common.HungerBridgeCommon;
import com.hungerbridge.common.util.Platform;
import com.hungerbridge.fabric.FabricPlatformAdapter;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin {

    private static final HungerBridgeCommon BRIDGE = new HungerBridgeCommon();

    @Inject(method = "setupServer", at = @At("RETURN"))
    private void hungerbridge$start(CallbackInfoReturnable<Boolean> cir) {
        DedicatedServer server = (DedicatedServer) (Object) this;

        Platform.setAdapter(new FabricPlatformAdapter());
        BRIDGE.start(server);
    }
}
