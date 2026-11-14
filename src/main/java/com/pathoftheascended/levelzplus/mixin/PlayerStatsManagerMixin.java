package com.pathoftheascended.levelzplus.mixin;

import com.pathoftheascended.levelzplus.CritBridge;
import com.pathoftheascended.levelzplus.ResourceBridge;
import net.levelz.stats.PlayerStatsManager;
import net.levelz.stats.Skill;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.levelz.stats.PlayerStatsManager", remap = false)
public abstract class PlayerStatsManagerMixin {
   @Inject(method = "setSkillLevel", at = @At("RETURN"), remap = false)
   private void levelzplus$refreshAfterSkillChange(Skill var1, int var2, CallbackInfo var3) {
      PlayerStatsManager var4 = (PlayerStatsManager)(Object)this;
      if (var1 == Skill.LUCK) {
         CritBridge.refresh(var4, var2);
      }

      if (var1 == Skill.STRENGTH || var1 == Skill.AGILITY || var1 == Skill.STAMINA || var1 == Skill.ARCHERY || var1 == Skill.ALCHEMY) {
         ResourceBridge.refresh(var4);
      }
   }

   @Inject(method = "readNbt", at = @At("RETURN"), remap = false)
   private void levelzplus$refreshAfterLoad(NbtCompound var1, CallbackInfo var2) {
      PlayerStatsManager var3 = (PlayerStatsManager)(Object)this;
      ResourceBridge.refresh(var3);
   }
}
