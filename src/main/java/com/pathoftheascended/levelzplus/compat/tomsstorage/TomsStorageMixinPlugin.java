package com.pathoftheascended.levelzplus.compat.tomsstorage;

import com.pathoftheascended.levelzplus.CompatConfig;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class TomsStorageMixinPlugin implements IMixinConfigPlugin {
   private boolean active;

   public void onLoad(String var1) {
      CompatConfig.load();
      boolean var2 = CompatConfig.moduleTomsStorage();
      boolean var3 = FabricLoader.getInstance().isModLoaded("toms_storage");
      this.active = var2 && var3;
      if (CompatConfig.debug()) {
         System.out.println("[LevelZ+] [Modules] Tom's Storage: configured=" + var2 + ", installed=" + var3 + ", active=" + this.active);
      }
   }

   public String getRefMapperConfig() {
      return null;
   }

   public boolean shouldApplyMixin(String var1, String var2) {
      return this.active;
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
