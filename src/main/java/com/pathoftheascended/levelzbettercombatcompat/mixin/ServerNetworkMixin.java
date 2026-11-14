package com.pathoftheascended.levelzbettercombatcompat.mixin;

import com.pathoftheascended.levelzbettercombatcompat.LevelZRestrictionChecker;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.api.WeaponAttributes.Attack;
import net.bettercombat.network.ServerNetwork;
import net.bettercombat.network.Packets.C2S_AttackRequest;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerNetwork.class, remap = false)
public abstract class ServerNetworkMixin {
   @Inject(method = "lambda$initializeHandlers$5", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
   private static void levelzBetterCombatCompat$validateOffhandAttackRequest(
      ServerPlayerEntity var0,
      C2S_AttackRequest var1,
      WeaponAttributes var2,
      Attack var3,
      AttackHand var4,
      ServerWorld var5,
      boolean var6,
      ServerPlayNetworkHandler var7,
      CallbackInfo var8
   ) {
      if (var4 != null && var4.isOffHand()) {
         if (LevelZRestrictionChecker.checkAttack(var0, var4.itemStack()).locked()) {
            var8.cancel();
         }
      }
   }
}
