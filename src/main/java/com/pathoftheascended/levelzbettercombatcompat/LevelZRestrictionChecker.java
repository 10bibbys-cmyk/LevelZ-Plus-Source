package com.pathoftheascended.levelzbettercombatcompat;

import java.util.ArrayList;
import java.util.Locale;
import net.levelz.data.LevelLists;
import net.levelz.init.ConfigInit;
import net.levelz.stats.PlayerStatsManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.Registries;

public final class LevelZRestrictionChecker {
   private LevelZRestrictionChecker() {
   }

   public static LevelZRestrictionChecker.Restriction checkAttack(PlayerEntity var0, ItemStack var1) {
      if (var0 != null && var1 != null && ConfigInit.CONFIG.lockedHandUsage) {
         Item var2 = var1.getItem();
         if (var2 == null) {
            return LevelZRestrictionChecker.Restriction.allowed();
         } else {
            String var3 = Registries.ITEM.getId(var2).toString();
            ArrayList var4 = LevelLists.customItemList;
            if (!var4.isEmpty() && var4.contains(var3)) {
               return checkList(var0, var4, var3);
            } else if (!(var2 instanceof ToolItem var5)) {
               return LevelZRestrictionChecker.Restriction.allowed();
            } else {
               ArrayList var6 = null;
               if (var2 instanceof SwordItem) {
                  var6 = LevelLists.swordList;
               } else if (var2 instanceof AxeItem) {
                  var6 = ConfigInit.CONFIG.bindAxeDamageToSwordRestriction ? LevelLists.swordList : LevelLists.axeList;
               } else if (var2 instanceof HoeItem) {
                  var6 = LevelLists.hoeList;
               } else if (var2 instanceof PickaxeItem || var2 instanceof ShovelItem) {
                  var6 = LevelLists.toolList;
               }

               if (var6 != null && var5.getMaterial() != null) {
                  String var7 = var5.getMaterial().toString().toLowerCase(Locale.ROOT);
                  return checkList(var0, var6, var7);
               } else {
                  return LevelZRestrictionChecker.Restriction.allowed();
               }
            }
         }
      } else {
         return LevelZRestrictionChecker.Restriction.allowed();
      }
   }

   private static LevelZRestrictionChecker.Restriction checkList(PlayerEntity var0, ArrayList<Object> var1, String var2) {
      if (!var1.contains(var2)) {
         return LevelZRestrictionChecker.Restriction.allowed();
      } else if (PlayerStatsManager.playerLevelisHighEnough(var0, var1, var2, true)) {
         return LevelZRestrictionChecker.Restriction.allowed();
      } else {
         int var3 = var1.indexOf(var2);
         if (var3 >= 0 && var3 + 2 < var1.size()) {
            String var4 = String.valueOf(var1.get(var3 + 1));
            Object var5 = var1.get(var3 + 2);
            return new LevelZRestrictionChecker.Restriction(true, "item.levelz." + var4 + ".tooltip", var5);
         } else {
            return LevelZRestrictionChecker.Restriction.allowed();
         }
      }
   }

   public record Restriction(boolean locked, String translationKey, Object requiredLevel) {
      private static final LevelZRestrictionChecker.Restriction ALLOWED = new LevelZRestrictionChecker.Restriction(false, "", 0);

      public static LevelZRestrictionChecker.Restriction allowed() {
         return ALLOWED;
      }
   }
}
