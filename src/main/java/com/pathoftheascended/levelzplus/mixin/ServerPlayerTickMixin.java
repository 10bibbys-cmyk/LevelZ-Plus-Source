package com.pathoftheascended.levelzplus.mixin;

import com.pathoftheascended.levelzplus.ResourceBridge;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_3222", remap = false)
public abstract class ServerPlayerTickMixin {
   @Unique
   private int levelzplus$syncTicks;

   @Inject(method = "method_5773()V", at = @At("TAIL"), require = 1, remap = false)
   private void levelzplus$keepAttributesInSync(CallbackInfo var1) {
      if (++this.levelzplus$syncTicks >= 100) {
         this.levelzplus$syncTicks = 0;
         PlayerEntity var2 = (PlayerEntity)(Object)this;
         ResourceBridge.refresh(var2);
      }
   }
}
