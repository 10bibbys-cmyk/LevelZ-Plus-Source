package com.pathoftheascended.levelzplus;

import java.lang.reflect.Field;
import net.levelz.access.PlayerStatsManagerAccess;
import net.levelz.stats.PlayerStatsManager;
import net.levelz.stats.Skill;
import net.minecraft.entity.player.PlayerEntity;

public final class DietBridge {
   private static volatile Field trackerPlayerField;
   private static volatile boolean fieldLookupAttempted;

   private DietBridge() {
   }

   public static Object scaleGain(Object var0, Object var1) {
      if (var1 instanceof Float var2 && var0 != null && !(var2 <= 0.0F)) {
         PlayerEntity var3 = resolvePlayer(var0);
         if (var3 == null) {
            return var1;
         }

         int var4 = 0;

         try {
            if (var3 instanceof PlayerStatsManagerAccess var5) {
               PlayerStatsManager var6 = var5.getPlayerStatsManager();
               if (var6 != null) {
                  var4 = Math.max(0, var6.getSkillLevel(Skill.STAMINA));
               }
            }
         } catch (Throwable var7) {
         }

         if (var4 <= 0) {
            return var1;
         }

         double var8 = 1.0 + var4 * CompatConfig.foodEffectivenessPerEnduranceLevel();
         return (float)(var2.floatValue() * var8);
      } else {
         return var1;
      }
   }

   private static PlayerEntity resolvePlayer(Object var0) {
      try {
         Field var1 = trackerPlayerField;
         if (var1 == null && !fieldLookupAttempted) {
            synchronized (DietBridge.class) {
               var1 = trackerPlayerField;
               if (var1 == null && !fieldLookupAttempted) {
                  fieldLookupAttempted = true;
                  var1 = var0.getClass().getDeclaredField("player");
                  var1.setAccessible(true);
                  trackerPlayerField = var1;
               }
            }
         }

         return (var1 == null ? null : var1.get(var0)) instanceof PlayerEntity var3 ? var3 : null;
      } catch (Throwable var5) {
         return null;
      }
   }
}
