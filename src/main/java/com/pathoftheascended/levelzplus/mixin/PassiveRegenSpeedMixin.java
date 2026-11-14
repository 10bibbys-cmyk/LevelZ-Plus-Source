package com.pathoftheascended.levelzplus.mixin;

import com.pathoftheascended.levelzplus.HealthRegenBridge;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "io.github.miche.passiveregen.PassiveRegenHandler", remap = false)
public abstract class PassiveRegenSpeedMixin {
   @Inject(method = "getSpeedBonusMultipliers", at = @At("RETURN"), require = 0)
   private void levelzplus$appendHealthSpeed(ServerPlayerEntity var1, int var2, boolean var3, CallbackInfoReturnable<List<Double>> var4) {
      List var5 = (List)var4.getReturnValue();
      if (var5 != null) {
         double var6 = HealthRegenBridge.speedMultiplier(var1);
         if (var6 != 1.0) {
            var5.add(var6);
         }
      }
   }
}
