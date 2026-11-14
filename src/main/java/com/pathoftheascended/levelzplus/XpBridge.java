package com.pathoftheascended.levelzplus;

import java.util.concurrent.ThreadLocalRandom;
import net.levelz.access.PlayerStatsManagerAccess;
import net.levelz.stats.PlayerStatsManager;
import net.levelz.stats.Skill;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class XpBridge {
   private XpBridge() {
   }

   public static int scale(PlayerEntity var0, int var1) {
      if (var1 > 0 && !XpStorageBridge.shouldBypassXpScaling(var0) && var0 instanceof ServerPlayerEntity && var0 instanceof PlayerStatsManagerAccess var2) {
         try {
            PlayerStatsManager var3 = var2.getPlayerStatsManager();
            if (var3 == null) {
               return var1;
            }

            int var4 = Math.max(0, var3.getSkillLevel(Skill.LUCK));
            if (var4 <= 0) {
               return var1;
            }

            double var5 = var1 * (1.0 + var4 * CompatConfig.xpGainPerLuckLevel());
            int var7 = (int)Math.floor(var5);
            double var8 = var5 - var7;
            if (var8 > 0.0 && ThreadLocalRandom.current().nextDouble() < var8) {
               var7++;
            }

            return Math.max(var1, var7);
         } catch (Throwable var10) {
            return var1;
         }
      } else {
         return var1;
      }
   }
}
