package com.pathoftheascended.levelzplus.mixin;

import com.pathoftheascended.levelzplus.XpBridge;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public abstract class PlayerXpMixin {
   @ModifyVariable(method = "method_7255", at = @At("HEAD"), argsOnly = true)
   private int levelzplus$scalePositiveXp(int var1) {
      return XpBridge.scale((PlayerEntity)(Object)this, var1);
   }
}
