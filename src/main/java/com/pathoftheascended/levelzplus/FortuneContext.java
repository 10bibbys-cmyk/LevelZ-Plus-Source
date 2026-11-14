package com.pathoftheascended.levelzplus;

import java.util.ArrayDeque;
import java.util.concurrent.ThreadLocalRandom;
import net.levelz.access.PlayerStatsManagerAccess;
import net.levelz.stats.PlayerStatsManager;
import net.levelz.stats.Skill;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public final class FortuneContext {
   private static final ThreadLocal<ArrayDeque<Integer>> BONUS_STACK = ThreadLocal.withInitial(ArrayDeque::new);

   private FortuneContext() {
   }

   public static void begin(Entity var0) {
      int var1 = 0;
      if (var0 instanceof PlayerEntity var2 && var2 instanceof PlayerStatsManagerAccess var3) {
         try {
            PlayerStatsManager var4 = var3.getPlayerStatsManager();
            if (var4 != null) {
               int var5 = Math.max(0, var4.getSkillLevel(Skill.LUCK));
               double var6 = var5 * CompatConfig.fortunePerLuckLevel();
               int var8 = (int)Math.floor(var6);
               double var9 = var6 - var8;
               var1 = var8;
               if (var9 > 0.0 && ThreadLocalRandom.current().nextDouble() < var9) {
                  var1++;
               }
            }
         } catch (Throwable var11) {
         }
      }

      BONUS_STACK.get().push(var1);
   }

   public static void end() {
      ArrayDeque var0 = BONUS_STACK.get();
      if (!var0.isEmpty()) {
         var0.pop();
      }

      if (var0.isEmpty()) {
         BONUS_STACK.remove();
      }
   }

   public static int currentBonus() {
      ArrayDeque var0 = BONUS_STACK.get();
      return var0.isEmpty() ? 0 : Math.max(0, (Integer)var0.peek());
   }
}
