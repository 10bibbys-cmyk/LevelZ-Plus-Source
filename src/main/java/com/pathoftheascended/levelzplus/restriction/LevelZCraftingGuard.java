package com.pathoftheascended.levelzplus.restriction;

import net.levelz.stats.PlayerStatsManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public final class LevelZCraftingGuard {
   private static final int LEVELZ_CRAFTING_CATEGORY = 4;

   private LevelZCraftingGuard() {
   }

   public static boolean isBlocked(PlayerEntity var0, ItemStack var1) {
      if (var0 == null || var1 == null || var1.isEmpty()) {
         return false;
      }

      if (var0.isCreative()) {
         return false;
      }

      int var2 = Registries.ITEM.getRawId(var1.getItem());
      return PlayerStatsManager.listContainsItemOrBlock(var0, var2, 4);
   }
}
