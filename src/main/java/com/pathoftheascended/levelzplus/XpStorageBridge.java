package com.pathoftheascended.levelzplus;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import net.minecraft.entity.player.PlayerEntity;

public final class XpStorageBridge {
   private static final long WITHDRAWAL_BYPASS_NANOS = TimeUnit.SECONDS.toNanos(5L);
   private static final Map<PlayerEntity, Long> BYPASS_UNTIL = Collections.synchronizedMap(new WeakHashMap<>());

   private XpStorageBridge() {
   }

   public static void armBookWithdrawal(PlayerEntity var0) {
      if (var0 != null) {
         BYPASS_UNTIL.put(var0, System.nanoTime() + WITHDRAWAL_BYPASS_NANOS);
      }
   }

   public static boolean shouldBypassXpScaling(PlayerEntity var0) {
      if (var0 == null) {
         return false;
      }

      Long var1 = BYPASS_UNTIL.get(var0);
      if (var1 == null) {
         return false;
      }

      if (System.nanoTime() <= var1) {
         return true;
      }

      BYPASS_UNTIL.remove(var0);
      return false;
   }
}
