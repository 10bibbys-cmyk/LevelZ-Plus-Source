package com.pathoftheascended.levelzbettercombatcompat.mixin.spellengine.client;

import com.pathoftheascended.levelzbettercombatcompat.LevelZRestrictionChecker;
import net.minecraft.util.Formatting;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.network.ClientPlayerEntity;
import net.spell_engine.internals.casting.SpellCast.Attempt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientPlayerEntity.class, priority = 900, remap = false)
public abstract class SpellEngineClientPlayerMixin {
   @Inject(
      method = "startSpellCast(Lnet/minecraft/class_1799;Lnet/minecraft/class_2960;)Lnet/spell_engine/internals/casting/SpellCast$Attempt;",
      at = @At("HEAD"),
      cancellable = true,
      require = 1,
      remap = false
   )
   private void levelzCombatCompat$checkSpellCastRequirement(ItemStack var1, Identifier var2, CallbackInfoReturnable<Attempt> var3) {
      ClientPlayerEntity var4 = (ClientPlayerEntity)(Object)this;
      LevelZRestrictionChecker.Restriction var5 = LevelZRestrictionChecker.checkAttack(var4, var1);
      if (var5.locked()) {
         var4.sendMessage(Text.translatable(var5.translationKey(), new Object[]{var5.requiredLevel()}).formatted(Formatting.RED), true);
         var3.setReturnValue(Attempt.none());
      }
   }
}
