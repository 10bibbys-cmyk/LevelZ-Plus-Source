package com.pathoftheascended.levelzplus.mixin;

import com.pathoftheascended.levelzplus.DietBridge;
import java.util.Map.Entry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.illusivesoulworks.diet.common.capability.PlayerDietTracker", remap = false)
public abstract class DietGainMixin {
   @Redirect(method = "apply", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getValue()Ljava/lang/Object;"), require = 0)
   private Object levelzplus$scalePositiveDietGain(Entry<?, ?> var1) {
      return DietBridge.scaleGain(this, var1.getValue());
   }
}
