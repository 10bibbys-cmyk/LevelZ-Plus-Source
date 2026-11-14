package com.pathoftheascended.levelzplus.mixin;

import com.pathoftheascended.levelzplus.CombatLifeAccess;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Immediately invalidates every pre-death mob credit, including unloaded mobs. */
@Mixin(ServerPlayerEntity.class)
public abstract class CombatDeathMixin {
   @Inject(method = "onDeath", at = @At("HEAD"))
   private void levelzplus$invalidateOnDeath(DamageSource source, CallbackInfo ci) {
      ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
      if (player.networkHandler != null) {
         ((CombatLifeAccess) player.networkHandler).levelzplus$invalidateCombatLife();
      }
   }
}
