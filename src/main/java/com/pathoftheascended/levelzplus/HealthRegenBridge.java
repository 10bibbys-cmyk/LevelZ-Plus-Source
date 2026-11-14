package com.pathoftheascended.levelzplus;

import net.levelz.access.PlayerStatsManagerAccess;
import net.levelz.stats.PlayerStatsManager;
import net.levelz.stats.Skill;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class HealthRegenBridge {
   private HealthRegenBridge() {
   }

   public static double speedMultiplier(ServerPlayerEntity var0) {
      if (var0 != null && var0 instanceof PlayerStatsManagerAccess var1) {
         try {
            PlayerStatsManager var2 = var1.getPlayerStatsManager();
            if (var2 == null) {
               return 1.0;
            }

            int var3 = Math.max(0, var2.getSkillLevel(Skill.HEALTH));
            return 1.0 + var3 * CompatConfig.healthRegenSpeedPerLevel();
         } catch (Throwable var4) {
            return 1.0;
         }
      } else {
         return 1.0;
      }
   }

   public static double speedMultiplier(PlayerEntity var0) {
      return var0 instanceof ServerPlayerEntity var1 ? speedMultiplier(var1) : 1.0;
   }
}
