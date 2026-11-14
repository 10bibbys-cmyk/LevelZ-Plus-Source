package com.pathoftheascended.levelzplus.mixin;

import com.pathoftheascended.levelzplus.CombatContributorAccess;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The dragon takes multipart damage through damagePart(), which may bypass
 * LivingEntity.damage(). Record these hits in the shared ledger as well.
 */
@Mixin(EnderDragonEntity.class)
public abstract class DragonDamageMixin {
   @Inject(method = "damagePart", at = @At("RETURN"))
   private void levelzplus$recordPartDamage(EnderDragonPart part, DamageSource source, float amount,
                                            CallbackInfoReturnable<Boolean> cir) {
      if (amount > 0.0F && Boolean.TRUE.equals(cir.getReturnValue())
            && !((EnderDragonEntity) (Object) this).getWorld().isClient()) {
         ((CombatContributorAccess) this).levelzplus$recordCombatHit(source);
      }
   }

   @Inject(method = "damage", at = @At("RETURN"))
   private void levelzplus$recordBodyDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
      if (amount > 0.0F && Boolean.TRUE.equals(cir.getReturnValue())
            && !((EnderDragonEntity) (Object) this).getWorld().isClient()) {
         ((CombatContributorAccess) this).levelzplus$recordCombatHit(source);
      }
   }
}
