package com.pathoftheascended.levelzplus.compat;

import com.pathoftheascended.levelzplus.CompatConfig;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class CompatModuleMixinPlugin implements IMixinConfigPlugin {
   private static final Set<String> REPORTED = Collections.newSetFromMap(new ConcurrentHashMap<>());

   public void onLoad(String var1) {
      CompatConfig.load();
   }

   public String getRefMapperConfig() {
      return null;
   }

   public boolean shouldApplyMixin(String var1, String var2) {
      String var3;
      String var4;
      boolean var5;
      if (var2.contains("levelztomsstorage")) {
         var3 = "Tom's Storage";
         var4 = "toms_storage";
         var5 = CompatConfig.moduleTomsStorage();
      } else if (var2.contains("levelzbettercombatcompat.mixin.spellengine")) {
         var3 = "Spell Engine";
         var4 = "spell_engine";
         var5 = CompatConfig.moduleSpellEngine();
      } else if (var2.contains("levelzbettercombatcompat.mixin")) {
         var3 = "Better Combat";
         var4 = "bettercombat";
         var5 = CompatConfig.moduleBetterCombat();
      } else {
         if (!var2.contains("levelzplus.compat.smallships.mixin")) {
            return true;
         }

         var3 = "Small Ships";
         var4 = "smallships";
         var5 = CompatConfig.moduleSmallShips();
      }

      boolean var6 = FabricLoader.getInstance().isModLoaded(var4);
      boolean var7 = var5 && var6;
      if (CompatConfig.debug() && REPORTED.add(var3)) {
         System.out.println("[LevelZ+] [Modules] " + var3 + ": configured=" + var5 + ", installed=" + var6 + ", active=" + var7);
      }

      return var7;
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
