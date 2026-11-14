package com.pathoftheascended.levelzbettercombatcompat.mixin.spellengine;

import com.pathoftheascended.levelzbettercombatcompat.LevelZRestrictionChecker;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.spell_engine.internals.SpellCastSyncHelper;
import net.spell_engine.network.Packets.SpellCastSync;
import net.spell_engine.network.Packets.SpellRequest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.spell_engine.network.ServerNetwork", priority = 900, remap = false)
public abstract class SpellEngineServerNetworkMixin {
   @Inject(method = "lambda$initializeHandlers$2", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
   private static void levelzCombatCompat$validateSpellCastSync(SpellCastSync var0, ServerPlayerEntity var1, CallbackInfo var2) {
      if (var0.spellId() != null) {
         if (LevelZRestrictionChecker.checkAttack(var1, var1.getMainHandStack()).locked()) {
            SpellCastSyncHelper.clearCasting(var1);
            var2.cancel();
         }
      }
   }

   @Inject(method = "lambda$initializeHandlers$5", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
   private static void levelzCombatCompat$validateSpellRelease(SpellRequest var0, ServerWorld var1, ServerPlayerEntity var2, CallbackInfo var3) {
      if (LevelZRestrictionChecker.checkAttack(var2, var2.getMainHandStack()).locked()) {
         SpellCastSyncHelper.clearCasting(var2);
         var3.cancel();
      }
   }
}
