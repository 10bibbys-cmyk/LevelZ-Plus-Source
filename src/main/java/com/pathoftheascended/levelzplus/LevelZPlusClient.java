package com.pathoftheascended.levelzplus;

import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.util.Formatting;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;

public final class LevelZPlusClient implements ClientModInitializer {
   private static final String[][] SKILL_KEYS = new String[][]{
      {"Health", "health"},
      {"Strength", "strength"},
      {"Agility", "agility"},
      {"Defense", "defense"},
      {"Stamina", "stamina"},
      {"Luck", "luck"},
      {"Archery", "archery"},
      {"Trade", "trade"},
      {"Farming", "farming"},
      {"Alchemy", "alchemy"}
   };

   public void onInitializeClient() {
      ItemTooltipCallback.EVENT.register(LevelZPlusClient::fixLevelZDynamicSkillNames);
   }

   private static void fixLevelZDynamicSkillNames(ItemStack var0, TooltipContext var1, List<Text> var2) {
      if (var2 != null && !var2.isEmpty()) {
         for (int var3 = 0; var3 < var2.size(); var3++) {
            Text var4 = (Text)var2.get(var3);
            if (var4 instanceof MutableText var5) {
               String var6;
               try {
                  var6 = stripLegacyFormatting(var5.getString());
               } catch (Throwable var8) {
                  continue;
               }

               MutableText var7 = replacementFor(var6);
               if (var7 != null) {
                  var2.set(var3, var7);
               }
            }
         }
      }
   }

   private static String stripLegacyFormatting(String var0) {
      if (var0 != null && var0.indexOf(167) >= 0) {
         StringBuilder var1 = new StringBuilder(var0.length());

         for (int var2 = 0; var2 < var0.length(); var2++) {
            char var3 = var0.charAt(var2);
            if (var3 == 167 && var2 + 1 < var0.length()) {
               var2++;
            } else {
               var1.append(var3);
            }
         }

         return var1.toString();
      } else {
         return var0;
      }
   }

   private static MutableText replacementFor(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         MutableText var1 = replaceDynamicSkillLine(var0, "Craftable: ", "item.levelz.crafting_restriction.tooltip");
         return var1 != null ? var1 : replaceDynamicSkillLine(var0, "Sheep Use: ", "item.levelz.sheep_restriction.tooltip");
      } else {
         return null;
      }
   }

   private static MutableText replaceDynamicSkillLine(String var0, String var1, String var2) {
      if (!var0.startsWith(var1)) {
         return null;
      }

      for (String[] var6 : SKILL_KEYS) {
         String var7 = var6[0];
         String var8 = var1 + var7 + " ";
         if (var0.startsWith(var8)) {
            String var9 = var0.substring(var8.length()).trim();

            int var10;
            try {
               var10 = Integer.parseInt(var9);
            } catch (NumberFormatException var12) {
               return null;
            }

            String var11 = localizedSkillName(var6[1], var7);
            return Text.translatable(var2, new Object[]{var11, var10}).formatted(Formatting.RED);
         }
      }

      return null;
   }

   private static String localizedSkillName(String var0, String var1) {
      String var2 = "spritetip.levelz." + var0 + "_skill";

      try {
         MutableText var3 = Text.translatable(var2, new Object[0]);
         String var4 = var3.getString();
         if (var4 != null && !var4.isEmpty() && !var4.equals(var2)) {
            return stripLegacyFormatting(var4).trim();
         }
      } catch (Throwable var5) {
      }

      return var1;
   }
}
