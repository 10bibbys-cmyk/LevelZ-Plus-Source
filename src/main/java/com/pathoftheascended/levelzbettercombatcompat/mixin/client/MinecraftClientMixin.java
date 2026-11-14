package com.pathoftheascended.levelzbettercombatcompat.mixin.client;

import com.pathoftheascended.levelzbettercombatcompat.LevelZRestrictionChecker;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.MinecraftClient_BetterCombat;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.PlayerAttackHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecraftClient.class, priority = 1100, remap = false)
public abstract class MinecraftClientMixin {
   @Inject(method = "startUpswing(Lnet/bettercombat/api/WeaponAttributes;)V", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
   private void levelzBetterCombatCompat$checkOffhandBeforeUpswing(WeaponAttributes weaponAttributes, CallbackInfo ci) {
      MinecraftClient client = (MinecraftClient)(Object)this;
      ClientPlayerEntity player = client.player;
      if (player == null) {
         return;
      }

      MinecraftClient_BetterCombat betterCombatClient = (MinecraftClient_BetterCombat)this;
      AttackHand attack = PlayerAttackHelper.getCurrentAttack(player, betterCombatClient.getComboCount());
      if (attack != null && attack.isOffHand()) {
         LevelZRestrictionChecker.Restriction restriction = LevelZRestrictionChecker.checkAttack(player, attack.itemStack());
         if (restriction.locked()) {
            player.sendMessage(
               Text.translatable(restriction.translationKey(), restriction.requiredLevel()).formatted(Formatting.RED),
               true
            );
            ci.cancel();
         }
      }
   }
}
