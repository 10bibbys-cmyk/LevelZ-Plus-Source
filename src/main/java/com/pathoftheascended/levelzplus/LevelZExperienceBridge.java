package com.pathoftheascended.levelzplus;

import net.levelz.access.PlayerSyncAccess;
import net.minecraft.server.network.ServerPlayerEntity;

public final class LevelZExperienceBridge {
   private LevelZExperienceBridge() {
   }

   public static boolean award(ServerPlayerEntity player, int amount, String reason) {
      if (player == null || amount <= 0 || !ProgressionXpConfig.independentXpReady()) {
         return false;
      }

      try {
         ((PlayerSyncAccess) player).addLevelExperience(amount);
         CompatConfig.debugLog("ProgressionXP", reason + " -> " + player.getEntityName() + " +" + amount);
         return true;
      } catch (Throwable error) {
         System.err.println("[LevelZ+] [ProgressionXP] Failed to award " + amount + " XP to "
            + player.getEntityName() + " for " + reason + ": " + rootMessage(error));
         return false;
      }
   }

   public static int scale(double amount, double multiplier) {
      if (amount <= 0.0 || multiplier <= 0.0) {
         return 0;
      }

      double scaled = amount * multiplier;
      if (!Double.isFinite(scaled) || scaled <= 0.0) {
         return 0;
      }
      if (scaled >= Integer.MAX_VALUE) {
         return Integer.MAX_VALUE;
      }
      return (int) Math.floor(scaled);
   }

   private static String rootMessage(Throwable throwable) {
      Throwable root = throwable;
      while (root.getCause() != null) {
         root = root.getCause();
      }
      String message = root.getMessage();
      return root.getClass().getSimpleName() + (message == null ? "" : ": " + message);
   }
}
