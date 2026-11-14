package com.pathoftheascended.levelzplus;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.levelz.access.PlayerStatsManagerAccess;
import net.levelz.stats.PlayerStatsManager;
import net.levelz.stats.Skill;
import net.minecraft.entity.player.PlayerEntity;

public final class FoodBridge {
   private static final ThreadLocal<ArrayDeque<PlayerEntity>> EATERS = ThreadLocal.withInitial(ArrayDeque::new);
   private static final Map<PlayerEntity, Double> FRACTIONS = Collections.synchronizedMap(new WeakHashMap<>());

   private FoodBridge() {
   }

   public static void begin(PlayerEntity var0) {
      if (var0 != null) {
         EATERS.get().push(var0);
      }
   }

   public static void end() {
      ArrayDeque var0 = EATERS.get();
      if (!var0.isEmpty()) {
         var0.pop();
      }

      if (var0.isEmpty()) {
         EATERS.remove();
      }
   }

   public static int scaleNutrition(int var0) {
      if (var0 <= 0) {
         return var0;
      }

      ArrayDeque var1 = EATERS.get();
      if (var1.isEmpty()) {
         return var0;
      }

      PlayerEntity var2 = (PlayerEntity)var1.peek();
      if (var2 instanceof PlayerStatsManagerAccess var3) {
         try {
            PlayerStatsManager var4 = var3.getPlayerStatsManager();
            if (var4 == null) {
               return var0;
            }

            int var5 = Math.max(0, var4.getSkillLevel(Skill.STAMINA));
            if (var5 <= 0) {
               return var0;
            }

            double var6 = var0 * (1.0 + var5 * CompatConfig.foodEffectivenessPerEnduranceLevel());
            int var8 = (int)Math.floor(var6);
            double var9 = var6 - var8;
            synchronized (FRACTIONS) {
               double var12 = FRACTIONS.getOrDefault(var2, 0.0) + var9;
               if (var12 >= 1.0) {
                  int var14 = (int)Math.floor(var12);
                  var8 += var14;
                  var12 -= var14;
               }

               if (var12 == 0.0) {
                  FRACTIONS.remove(var2);
               } else {
                  FRACTIONS.put(var2, var12);
               }
            }

            return Math.max(var0, var8);
         } catch (Throwable var17) {
            return var0;
         }
      } else {
         return var0;
      }
   }
}
