package dev.levelztomsstorage.compat;

import com.pathoftheascended.levelzplus.CompatConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class LevelZCraftingGuard {
   private LevelZCraftingGuard() {
   }

   public static boolean isBlocked(PlayerEntity var0, ItemStack var1) {
      boolean var2 = com.pathoftheascended.levelzplus.restriction.LevelZCraftingGuard.isBlocked(var0, var1);
      if (CompatConfig.debug()) {
         System.out.println("[LevelZ+] [TomsStorage] crafting check stack=" + var1 + ", result=" + (var2 ? "DENIED" : "ALLOWED"));
      }

      return var2;
   }
}
