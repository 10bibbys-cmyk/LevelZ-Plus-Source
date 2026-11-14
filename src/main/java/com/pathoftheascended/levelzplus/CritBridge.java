package com.pathoftheascended.levelzplus;

import net.levelz.stats.PlayerStatsManager;
import net.minecraft.entity.player.PlayerEntity;

public final class CritBridge {
   private CritBridge() {
   }

   public static void refresh(PlayerStatsManager var0) {
      ResourceBridge.refreshCrit(var0);
   }

   public static void refresh(PlayerStatsManager var0, int var1) {
      ResourceBridge.refreshCrit(var0, var1);
   }

   public static void refresh(PlayerEntity var0) {
      ResourceBridge.refreshCrit(var0);
   }

   public static void refresh(PlayerEntity var0, int var1) {
      ResourceBridge.refreshCrit(var0, var1);
   }
}
