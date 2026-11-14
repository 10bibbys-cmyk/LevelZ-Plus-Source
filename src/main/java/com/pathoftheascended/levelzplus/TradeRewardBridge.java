package com.pathoftheascended.levelzplus;

import java.util.concurrent.ThreadLocalRandom;
import net.levelz.access.PlayerStatsManagerAccess;
import net.levelz.stats.PlayerStatsManager;
import net.levelz.stats.Skill;
import net.minecraft.entity.player.PlayerEntity;

public final class TradeRewardBridge {
   private TradeRewardBridge() {
   }

   public static int scaleCurrencyAmount(PlayerEntity var0, String var1, int var2) {
      if (var0 != null && var2 > 0 && isNumismaticCoin(var1)) {
         int var3 = 0;

         try {
            if (var0 instanceof PlayerStatsManagerAccess var4) {
               PlayerStatsManager var5 = var4.getPlayerStatsManager();
               if (var5 != null) {
                  var3 = Math.max(0, var5.getSkillLevel(Skill.TRADE));
               }
            }
         } catch (Throwable var9) {
         }

         if (var3 <= 0) {
            return var2;
         }

         double var10 = var2 * (1.0 + var3 * CompatConfig.currencyGainPerTradeLevel());
         int var6 = (int)Math.floor(var10);
         double var7 = var10 - var6;
         if (var7 > 0.0 && ThreadLocalRandom.current().nextDouble() < var7) {
            var6++;
         }

         return Math.max(var2, var6);
      } else {
         return var2;
      }
   }

   public static boolean isNumismaticCoin(String var0) {
      return "numismatic-overhaul:bronze_coin".equals(var0) || "numismatic-overhaul:silver_coin".equals(var0) || "numismatic-overhaul:gold_coin".equals(var0);
   }
}
