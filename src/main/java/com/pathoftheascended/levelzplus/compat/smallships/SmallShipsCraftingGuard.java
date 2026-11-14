package com.pathoftheascended.levelzplus.compat.smallships;

import com.pathoftheascended.levelzplus.CompatConfig;
import com.pathoftheascended.levelzplus.restriction.LevelZCraftingGuard;
import com.talhanation.smallships.api.ShipType;
import java.util.Locale;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.entity.vehicle.BoatEntity.Type;

public final class SmallShipsCraftingGuard {
   private SmallShipsCraftingGuard() {
   }

   public static boolean shouldBlock(ServerPlayerEntity var0, ShipType var1, Type var2) {
      if (!CompatConfig.smallShipsEnforceCraftingRequirements()) {
         return false;
      }

      if (var0 != null && var1 != null && var2 != null) {
         String var3 = var1.getId() == null ? "" : var1.getId().toString();
         String var4 = sanitizeWood(var2.getName());
         String var5 = resolveItemId(var3, var4);
         if (var5 != null && !var5.isBlank()) {
            try {
               Identifier var6 = new Identifier(var5);
               Item var7 = Registries.ITEM.get(var6);
               String var8 = String.valueOf(Registries.ITEM.getId(var7));
               if (!var5.equals(var8)) {
                  return unresolved("resolved registry item was " + var8, var3, var5);
               }

               ItemStack var9 = new ItemStack(var7);
               boolean var10 = LevelZCraftingGuard.isBlocked(var0, var9);
               CompatConfig.debugLog("SmallShips", "ship=" + var3 + ", wood=" + var4 + ", item=" + var5 + ", result=" + (var10 ? "DENIED" : "ALLOWED"));
               if (var10 && CompatConfig.smallShipsShowDenialMessage()) {
                  String var11 = pathOf(var3).replace('_', ' ');
                  String var12 = CompatConfig.smallShipsDenialMessage().replace("{ship}", var11).replace("{item}", var5);
                  var0.sendMessage(Text.literal(var12), true);
               }

               return var10;
            } catch (Throwable var13) {
               return unresolved(var13.getClass().getSimpleName() + (var13.getMessage() == null ? "" : ": " + var13.getMessage()), var3, var5);
            }
         } else {
            return unresolved("no item mapping", var3, null);
         }
      } else {
         return unresolved("missing player/ship/wood context", null, null);
      }
   }

   private static String resolveItemId(String var0, String var1) {
      String var2 = namespaceOf(var0);
      String var3 = pathOf(var0);
      if (!var2.isEmpty() && !var3.isEmpty() && !var1.isEmpty()) {
         Map var4 = CompatConfig.smallShipsItemOverrides();
         String var5 = (String)var4.get(var0);
         if (var5 == null || var5.isBlank()) {
            var5 = "{namespace}:{wood}_{ship}";
         }

         return var5.replace("{namespace}", var2).replace("{wood}", var1).replace("{ship}", var3);
      } else {
         return null;
      }
   }

   private static boolean unresolved(String var0, String var1, String var2) {
      boolean var3 = !CompatConfig.smallShipsFailOpen();
      CompatConfig.debugLog(
         "SmallShips",
         "unresolved mapping: ship=" + var1 + ", item=" + var2 + ", reason=" + var0 + ", result=" + (var3 ? "DENIED (fail-closed)" : "ALLOWED (fail-open)")
      );
      return var3;
   }

   private static String sanitizeWood(String var0) {
      return var0 == null ? "" : var0.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
   }

   private static String namespaceOf(String var0) {
      int var1 = var0.indexOf(58);
      return var1 < 0 ? "smallships" : var0.substring(0, var1);
   }

   private static String pathOf(String var0) {
      int var1 = var0.indexOf(58);
      return var1 < 0 ? var0 : var0.substring(var1 + 1);
   }
}
