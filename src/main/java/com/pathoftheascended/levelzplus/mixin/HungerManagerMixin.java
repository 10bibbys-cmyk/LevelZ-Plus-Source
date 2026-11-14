package com.pathoftheascended.levelzplus.mixin;

import com.pathoftheascended.levelzplus.FoodBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.minecraft.class_1702", remap = false)
public abstract class HungerManagerMixin {
   @ModifyVariable(method = "method_7585(IF)V", at = @At("HEAD"), argsOnly = true, require = 1, remap = false)
   private int levelzplus$scaleFoodNutrition(int var1) {
      return FoodBridge.scaleNutrition(var1);
   }
}
