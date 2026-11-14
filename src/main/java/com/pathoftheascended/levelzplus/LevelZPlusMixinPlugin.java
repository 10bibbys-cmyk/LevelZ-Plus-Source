package com.pathoftheascended.levelzplus;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class LevelZPlusMixinPlugin implements IMixinConfigPlugin {
   public void onLoad(String var1) {
   }

   public String getRefMapperConfig() {
      return null;
   }

   public boolean shouldApplyMixin(String var1, String var2) {
      FabricLoader var3 = FabricLoader.getInstance();
      if (var2.endsWith("PassiveRegenSpeedMixin")) {
         return var3.isModLoaded("passiveregen");
      } else if (var2.endsWith("DietGainMixin")) {
         return var3.isModLoaded("diet");
      } else if (var2.endsWith("BountifulRewardMixin")) {
         return var3.isModLoaded("bountiful");
      } else if (var2.endsWith("XpStorageBookMixin")) {
         return var3.isModLoaded("xp_storage");
      } else if (var2.endsWith("ActuallyHarvestXpMixin")) {
         return var3.isModLoaded("actuallyharvest");
      } else {
         return true;
      }
   }

   public void acceptTargets(Set<String> var1, Set<String> var2) {
   }

   public List<String> getMixins() {
      return null;
   }

   public void preApply(String var1, ClassNode var2, String var3, IMixinInfo var4) {
   }

   public void postApply(String var1, ClassNode var2, String var3, IMixinInfo var4) {
   }
}
