package com.hungerbridge.fabric.mixin;

import net.minecraft.class_3176;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(class_3176.class)
public class PrintClassNameMixin {
    static {
        System.out.println("[HungerBridge/DIAG] class_3176 = " + class_3176.class.getName());
    }
}
