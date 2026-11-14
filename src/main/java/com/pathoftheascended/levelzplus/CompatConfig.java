package com.pathoftheascended.levelzplus;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;

public final class CompatConfig {
   private static final String CONFIG_NAME = "levelz_plus.properties";
   private static final String LEGACY_CONFIG_NAME = "levelz_plus.json";
   private static volatile boolean loaded;
   private static double healthRegenSpeedPerLevel = 0.01;
   private static double meleeStaminaCostReductionPerLevel = 0.005;
   private static double rollDistancePerAgilityLevel = 0.02;
   private static double rollRechargePerAgilityLevel = 0.1;
   private static int[] extraRollThresholds = new int[]{10, 30, 50};
   private static double maxStaminaPerEnduranceLevel = 1.0;
   private static double staminaRegenPerEnduranceLevel = 0.05;
   private static double foodEffectivenessPerEnduranceLevel = 0.005;
   private static double fortunePerLuckLevel = 0.05;
   private static double zenithCritChancePerLuckLevel = 0.001;
   private static double xpGainPerLuckLevel = 0.005;
   private static double zenithDrawSpeedPerArcheryLevel = 0.005;
   private static double currencyGainPerTradeLevel = 0.005;
   private static double maxManaPerAlchemyLevel = 1.0;
   private static double manaRegenPerAlchemyLevel = 0.04;
   private static boolean debug = false;
   private static boolean moduleTomsStorage = true;
   private static boolean moduleBetterCombat = true;
   private static boolean moduleSpellEngine = true;
   private static boolean moduleSmallShips = true;
   private static boolean smallShipsEnforceCraftingRequirements = true;
   private static boolean smallShipsShowDenialMessage = true;
   private static boolean smallShipsFailOpen = true;
   private static String smallShipsDenialMessage = "You do not meet the LevelZ crafting requirement for {ship}.";
   private static Map<String, String> smallShipsItemOverrides = Collections.emptyMap();

   private CompatConfig() {
   }

   public static synchronized void load() {
      if (!loaded) {
         loaded = true;
         Path var0 = FabricLoader.getInstance().getConfigDir();
         Path var1 = var0.resolve("levelz_plus.properties");
         Path var2 = var0.resolve("levelz_plus.json");

         try {
            Files.createDirectories(var0);
            boolean var3 = false;
            if (!Files.exists(var1)) {
               if (Files.exists(var2)) {
                  migrateLegacyJson(var2);
                  var3 = true;
               }

               Files.writeString(var1, annotatedConfig(var3), StandardCharsets.UTF_8);
            }

            Properties var4 = new Properties();

            try (BufferedReader var5 = Files.newBufferedReader(var1, StandardCharsets.UTF_8)) {
               var4.load(var5);
            }

            appendProgressionDefaultsIfMissing(var1, var4);
            apply(var4);
         } catch (Exception var10) {
            System.err.println("[LevelZ+] Failed to load levelz_plus.properties; using safe defaults. " + rootMessage(var10));
         }
      }
   }

   private static void appendProgressionDefaultsIfMissing(Path configPath, Properties properties) {
      LinkedHashMap<String, String> defaults = new LinkedHashMap<>();
      defaults.put("modules.actually_harvest", "true");
      defaults.put("levelz_xp.combat.enabled", "false");
      defaults.put("levelz_xp.combat.fail_safe_enabled", "false");
      defaults.put("levelz_xp.combat.reward_percent_per_contributor", "100");
      defaults.put("levelz_xp.combat.mob_xp_multiplier", "1.0");
      defaults.put("levelz_xp.combat.dragon_xp_multiplier", "0.5");
      defaults.put("levelz_xp.combat.max_health_fraction", "0.10");
      defaults.put("levelz_xp.actually_harvest.enabled", "true");
      defaults.put("levelz_xp.actually_harvest.multiplier", "1.0");

      StringBuilder missing = new StringBuilder();
      for (Map.Entry<String, String> entry : defaults.entrySet()) {
         if (!properties.containsKey(entry.getKey())) {
            missing.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
            properties.setProperty(entry.getKey(), entry.getValue());
         }
      }

      if (missing.length() == 0) {
         return;
      }

      StringBuilder block = new StringBuilder(900);
      block.append("\n# ============================================================\n");
      block.append("# LevelZ+ 2.1.2 progression XP settings (added automatically)\n");
      block.append("# Existing values above were preserved; only missing keys were added.\n");
      block.append("# Combat LevelZ XP requires LevelZ useIndependentExp=true.\n");
      block.append("# To use shared mob XP, set native LevelZ mobXPMultiplier=0.0\n");
      block.append("# and enable levelz_xp.combat.enabled=true.\n");
      block.append("# For shared dragons, set native dragonXPMultiplier=0.0.\n");
      block.append("# ============================================================\n");
      block.append(missing);

      try {
         Files.writeString(
            configPath,
            block.toString(),
            StandardCharsets.UTF_8,
            StandardOpenOption.APPEND
         );
         System.out.println("[LevelZ+] Added missing progression XP options to levelz_plus.properties without overwriting existing values.");
      } catch (Exception error) {
         System.err.println("[LevelZ+] Could not append missing progression XP options to levelz_plus.properties; "
            + "existing values remain loaded and runtime defaults will be used for missing keys. " + rootMessage(error));
      }
   }

   private static void apply(Properties var0) {
      debug = readBoolean(var0, "general.debug", debug);
      moduleTomsStorage = readBoolean(var0, "modules.toms_storage", moduleTomsStorage);
      moduleBetterCombat = readBoolean(var0, "modules.better_combat", moduleBetterCombat);
      moduleSpellEngine = readBoolean(var0, "modules.spell_engine", moduleSpellEngine);
      moduleSmallShips = readBoolean(var0, "modules.small_ships", moduleSmallShips);
      healthRegenSpeedPerLevel = readDouble(var0, "stats.endurance.health_regen_speed_per_level", healthRegenSpeedPerLevel);
      meleeStaminaCostReductionPerLevel = readDouble(var0, "stats.endurance.melee_stamina_cost_reduction_per_level", meleeStaminaCostReductionPerLevel);
      rollDistancePerAgilityLevel = readDouble(var0, "stats.agility.roll_distance_per_level", rollDistancePerAgilityLevel);
      rollRechargePerAgilityLevel = readDouble(var0, "stats.agility.roll_recharge_per_level", rollRechargePerAgilityLevel);
      extraRollThresholds = readThresholds(var0.getProperty("stats.agility.extra_roll_thresholds"), extraRollThresholds);
      maxStaminaPerEnduranceLevel = readDouble(var0, "stats.endurance.max_stamina_per_level", maxStaminaPerEnduranceLevel);
      staminaRegenPerEnduranceLevel = readDouble(var0, "stats.endurance.stamina_regen_per_level", staminaRegenPerEnduranceLevel);
      foodEffectivenessPerEnduranceLevel = readDouble(var0, "stats.endurance.food_effectiveness_per_level", foodEffectivenessPerEnduranceLevel);
      fortunePerLuckLevel = readDouble(var0, "stats.luck.fortune_per_level", fortunePerLuckLevel);
      zenithCritChancePerLuckLevel = readDouble(var0, "stats.luck.zenith_crit_chance_per_level", zenithCritChancePerLuckLevel);
      xpGainPerLuckLevel = readDouble(var0, "stats.luck.xp_gain_per_level", xpGainPerLuckLevel);
      zenithDrawSpeedPerArcheryLevel = readDouble(var0, "stats.archery.zenith_draw_speed_per_level", zenithDrawSpeedPerArcheryLevel);
      currencyGainPerTradeLevel = readDouble(var0, "stats.trade.currency_gain_per_level", currencyGainPerTradeLevel);
      maxManaPerAlchemyLevel = readDouble(var0, "stats.alchemy.max_mana_per_level", maxManaPerAlchemyLevel);
      manaRegenPerAlchemyLevel = readDouble(var0, "stats.alchemy.mana_regen_per_level", manaRegenPerAlchemyLevel);
      smallShipsEnforceCraftingRequirements = readBoolean(var0, "small_ships.enforce_crafting_requirements", smallShipsEnforceCraftingRequirements);
      smallShipsShowDenialMessage = readBoolean(var0, "small_ships.show_denial_message", smallShipsShowDenialMessage);
      smallShipsFailOpen = readBoolean(var0, "small_ships.fail_open", smallShipsFailOpen);
      smallShipsDenialMessage = var0.getProperty("small_ships.denial_message", smallShipsDenialMessage).trim();
      smallShipsItemOverrides = parseOverrides(var0.getProperty("small_ships.ship_item_overrides", ""));
   }

   private static void migrateLegacyJson(Path var0) {
      try {
         String var1 = Files.readString(var0, StandardCharsets.UTF_8);
         healthRegenSpeedPerLevel = legacyDouble(var1, "healthRegenSpeedPerLevel", healthRegenSpeedPerLevel);
         meleeStaminaCostReductionPerLevel = legacyDouble(var1, "meleeStaminaCostReductionPerLevel", meleeStaminaCostReductionPerLevel);
         rollDistancePerAgilityLevel = legacyDouble(var1, "rollDistancePerAgilityLevel", rollDistancePerAgilityLevel);
         rollRechargePerAgilityLevel = legacyDouble(var1, "rollRechargePerAgilityLevel", rollRechargePerAgilityLevel);
         maxStaminaPerEnduranceLevel = legacyDouble(var1, "maxStaminaPerEnduranceLevel", maxStaminaPerEnduranceLevel);
         staminaRegenPerEnduranceLevel = legacyDouble(var1, "staminaRegenPerEnduranceLevel", staminaRegenPerEnduranceLevel);
         foodEffectivenessPerEnduranceLevel = legacyDouble(var1, "foodEffectivenessPerEnduranceLevel", foodEffectivenessPerEnduranceLevel);
         fortunePerLuckLevel = legacyDouble(var1, "fortunePerLuckLevel", fortunePerLuckLevel);
         zenithCritChancePerLuckLevel = legacyDouble(var1, "zenithCritChancePerLuckLevel", zenithCritChancePerLuckLevel);
         xpGainPerLuckLevel = legacyDouble(var1, "xpGainPerLuckLevel", xpGainPerLuckLevel);
         zenithDrawSpeedPerArcheryLevel = legacyDouble(var1, "zenithDrawSpeedPerArcheryLevel", zenithDrawSpeedPerArcheryLevel);
         currencyGainPerTradeLevel = legacyDouble(var1, "currencyGainPerTradeLevel", currencyGainPerTradeLevel);
         maxManaPerAlchemyLevel = legacyDouble(var1, "maxManaPerAlchemyLevel", maxManaPerAlchemyLevel);
         manaRegenPerAlchemyLevel = legacyDouble(var1, "manaRegenPerAlchemyLevel", manaRegenPerAlchemyLevel);
         extraRollThresholds = legacyThresholds(var1, extraRollThresholds);
         System.out.println("[LevelZ+] Migrated legacy levelz_plus.json values into levelz_plus.properties.");
      } catch (Exception var2) {
         System.err.println("[LevelZ+] Could not migrate legacy levelz_plus.json; defaults will be used. " + rootMessage(var2));
      }
   }

   private static double legacyDouble(String var0, String var1, double var2) {
      Matcher var4 = Pattern.compile("\\\"" + Pattern.quote(var1) + "\\\"\\s*:\\s*(-?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?)").matcher(var0);
      if (!var4.find()) {
         return var2;
      }

      try {
         double var5 = Double.parseDouble(var4.group(1));
         return Double.isFinite(var5) ? var5 : var2;
      } catch (NumberFormatException var7) {
         return var2;
      }
   }

   private static int[] legacyThresholds(String var0, int[] var1) {
      Matcher var2 = Pattern.compile("\\\"extraRollThresholds\\\"\\s*:\\s*\\[([^]]*)]").matcher(var0);
      return !var2.find() ? var1 : readThresholds(var2.group(1), var1);
   }

   private static double readDouble(Properties var0, String var1, double var2) {
      String var4 = var0.getProperty(var1);
      if (var4 != null && !var4.isBlank()) {
         try {
            double var5 = Double.parseDouble(var4.trim());
            if (Double.isFinite(var5)) {
               return var5;
            }
         } catch (NumberFormatException var7) {
         }

         System.err.println("[LevelZ+] Invalid numeric config value for '" + var1 + "': " + var4 + ". Keeping " + var2 + ".");
         return var2;
      } else {
         return var2;
      }
   }

   private static boolean readBoolean(Properties var0, String var1, boolean var2) {
      String var3 = var0.getProperty(var1);
      if (var3 == null || var3.isBlank()) {
         return var2;
      }

      if (var3.equalsIgnoreCase("true")) {
         return true;
      }

      if (var3.equalsIgnoreCase("false")) {
         return false;
      }

      System.err.println("[LevelZ+] Invalid boolean config value for '" + var1 + "': " + var3 + ". Keeping " + var2 + ".");
      return var2;
   }

   private static int[] readThresholds(String var0, int[] var1) {
      if (var0 != null && !var0.isBlank()) {
         String var2 = var0.replace("[", "").replace("]", "");
         String[] var3 = var2.split(",");
         int[] var4 = new int[var3.length];

         try {
            int var5 = -1;

            for (int var6 = 0; var6 < var3.length; var6++) {
               int var7 = Integer.parseInt(var3[var6].trim());
               if (var7 < 0 || var7 <= var5) {
                  throw new NumberFormatException("thresholds must be ascending non-negative integers");
               }

               var4[var6] = var7;
               var5 = var7;
            }

            return var4.length == 0 ? var1 : var4;
         } catch (NumberFormatException var8) {
            System.err.println("[LevelZ+] Invalid stats.agility.extra_roll_thresholds: " + var0 + ". Keeping existing thresholds.");
            return var1;
         }
      } else {
         return var1;
      }
   }

   private static Map<String, String> parseOverrides(String var0) {
      if (var0 != null && !var0.isBlank()) {
         LinkedHashMap var1 = new LinkedHashMap();

         for (String var5 : var0.split(";")) {
            String var6 = var5.trim();
            if (!var6.isEmpty()) {
               int var7 = var6.indexOf(61);
               if (var7 > 0 && var7 < var6.length() - 1) {
                  var1.put(var6.substring(0, var7).trim(), var6.substring(var7 + 1).trim());
               } else {
                  System.err.println("[LevelZ+] Ignoring malformed Small Ships override: " + var6);
               }
            }
         }

         return Collections.unmodifiableMap(var1);
      } else {
         return Collections.emptyMap();
      }
   }

   private static String annotatedConfig(boolean var0) {
      StringBuilder var1 = new StringBuilder(5200);
      var1.append("# ============================================================\n");
      var1.append("# LevelZ+ configuration\n");
      var1.append("# ============================================================\n");
      var1.append("# LevelZ is the only required dependency. Optional compatibility\n");
      var1.append("# modules activate only when BOTH their toggle is true and their\n");
      var1.append("# target mod is installed. Changes require a game/server restart.\n");
      if (var0) {
         var1.append("# Existing stat values were migrated from levelz_plus.json.\n");
      }

      var1.append("#\n");
      var1.append("# Debug logging is intentionally off by default. Enable it while\n");
      var1.append("# diagnosing compatibility checks, then turn it back off.\n");
      var1.append("general.debug=false\n\n");
      var1.append("# -------------------- Optional compatibility modules --------------------\n");
      var1.append("modules.toms_storage=true\n");
      var1.append("modules.better_combat=true\n");
      var1.append("modules.spell_engine=true\n");
      var1.append("modules.small_ships=true\n");
      var1.append("modules.actually_harvest=true\n\n");
      var1.append("# -------------------- Existing LevelZ+ stat integrations ----------------\n");
      var1.append("# Set a per-level value to 0 to effectively disable that contribution.\n");
      var1.append("# Historical key name retained for config compatibility; this value scales LevelZ HEALTH.\n");
      var1.append("stats.endurance.health_regen_speed_per_level=").append(healthRegenSpeedPerLevel).append('\n');
      var1.append("stats.endurance.melee_stamina_cost_reduction_per_level=").append(meleeStaminaCostReductionPerLevel).append('\n');
      var1.append("stats.endurance.max_stamina_per_level=").append(maxStaminaPerEnduranceLevel).append('\n');
      var1.append("stats.endurance.stamina_regen_per_level=").append(staminaRegenPerEnduranceLevel).append('\n');
      var1.append("stats.endurance.food_effectiveness_per_level=").append(foodEffectivenessPerEnduranceLevel).append("\n\n");
      var1.append("stats.agility.roll_distance_per_level=").append(rollDistancePerAgilityLevel).append('\n');
      var1.append("stats.agility.roll_recharge_per_level=").append(rollRechargePerAgilityLevel).append('\n');
      var1.append("# Agility levels at which additional Combat Roll charges are granted.\n");
      var1.append("stats.agility.extra_roll_thresholds=").append(join(extraRollThresholds)).append("\n\n");
      var1.append("stats.luck.fortune_per_level=").append(fortunePerLuckLevel).append('\n');
      var1.append("stats.luck.zenith_crit_chance_per_level=").append(zenithCritChancePerLuckLevel).append('\n');
      var1.append("stats.luck.xp_gain_per_level=").append(xpGainPerLuckLevel).append("\n\n");
      var1.append("stats.archery.zenith_draw_speed_per_level=").append(zenithDrawSpeedPerArcheryLevel).append("\n\n");
      var1.append("stats.trade.currency_gain_per_level=").append(currencyGainPerTradeLevel).append("\n\n");
      var1.append("stats.alchemy.max_mana_per_level=").append(maxManaPerAlchemyLevel).append('\n');
      var1.append("stats.alchemy.mana_regen_per_level=").append(manaRegenPerAlchemyLevel).append("\n\n");
      var1.append("# -------------------- Independent LevelZ progression XP -----------------\n");
      var1.append("# Shared combat progression XP is opt-in to prevent duplicate native XP.\n");
      var1.append("# Set LevelZ useIndependentExp=true and mobXPMultiplier=0.0, then\n");
      var1.append("# enable combat XP below. For shared dragon XP, also set LevelZ\n");
      var1.append("# dragonXPMultiplier=0.0.\n");
      var1.append("# Contributors must survive and remain connected until mob death.\n");
      var1.append("levelz_xp.combat.enabled=false\n");
      var1.append("levelz_xp.combat.fail_safe_enabled=false\n");
      var1.append("levelz_xp.combat.reward_percent_per_contributor=100\n");
      var1.append("# Mob reward: base XP * multiplier + maximum health * fraction.\n");
      var1.append("levelz_xp.combat.mob_xp_multiplier=1.0\n");
      var1.append("# Shared Dragon XP multiplier (native Dragon XP must be disabled).\n");
      var1.append("levelz_xp.combat.dragon_xp_multiplier=0.5\n");
      var1.append("levelz_xp.combat.max_health_fraction=0.10\n\n");
      var1.append("# Actually Harvest retains vanilla XP and adds independent LevelZ XP.\n");
      var1.append("levelz_xp.actually_harvest.enabled=true\n");
      var1.append("levelz_xp.actually_harvest.multiplier=1.0\n\n");
      var1.append("# -------------------- Small Ships / Dockyard ----------------------------\n");
      var1.append("# When enabled, the Dockyard checks the LevelZ CRAFTING requirement of\n");
      var1.append("# the actual wood-specific ship item before construction starts. If the\n");
      var1.append("# item has no LevelZ crafting requirement, LevelZ+ does not block it.\n");
      var1.append("small_ships.enforce_crafting_requirements=true\n");
      var1.append("small_ships.show_denial_message=true\n");
      var1.append("# If a ship cannot be mapped to an item, true allows construction and\n");
      var1.append("# logs the problem in debug mode. false blocks unknown mappings.\n");
      var1.append("small_ships.fail_open=true\n");
      var1.append("# Supported placeholders: {ship}, {item}\n");
      var1.append("small_ships.denial_message=You do not meet the LevelZ crafting requirement for {ship}.\n");
      var1.append("# Optional addon mappings. Separate entries with ';'. Templates support\n");
      var1.append("# {wood}, {ship}, and {namespace}. Native Small Ships needs no override.\n");
      var1.append("# Example: myaddon:longship=myaddon:{wood}_longship\n");
      var1.append("small_ships.ship_item_overrides=\n");
      return var1.toString();
   }

   private static String join(int[] var0) {
      StringBuilder var1 = new StringBuilder();

      for (int var2 = 0; var2 < var0.length; var2++) {
         if (var2 > 0) {
            var1.append(',');
         }

         var1.append(var0[var2]);
      }

      return var1.toString();
   }

   private static void ensureLoaded() {
      if (!loaded) {
         load();
      }
   }

   public static boolean debug() {
      ensureLoaded();
      return debug;
   }

   public static boolean moduleTomsStorage() {
      ensureLoaded();
      return moduleTomsStorage;
   }

   public static boolean moduleBetterCombat() {
      ensureLoaded();
      return moduleBetterCombat;
   }

   public static boolean moduleSpellEngine() {
      ensureLoaded();
      return moduleSpellEngine;
   }

   public static boolean moduleSmallShips() {
      ensureLoaded();
      return moduleSmallShips;
   }

   public static boolean smallShipsEnforceCraftingRequirements() {
      ensureLoaded();
      return smallShipsEnforceCraftingRequirements;
   }

   public static boolean smallShipsShowDenialMessage() {
      ensureLoaded();
      return smallShipsShowDenialMessage;
   }

   public static boolean smallShipsFailOpen() {
      ensureLoaded();
      return smallShipsFailOpen;
   }

   public static String smallShipsDenialMessage() {
      ensureLoaded();
      return smallShipsDenialMessage;
   }

   public static Map<String, String> smallShipsItemOverrides() {
      ensureLoaded();
      return smallShipsItemOverrides;
   }

   public static void debugLog(String var0, String var1) {
      if (debug()) {
         System.out.println("[LevelZ+] [" + var0 + "] " + var1);
      }
   }

   public static double healthRegenSpeedPerLevel() {
      ensureLoaded();
      return healthRegenSpeedPerLevel;
   }

   public static double meleeStaminaCostReductionPerLevel() {
      ensureLoaded();
      return meleeStaminaCostReductionPerLevel;
   }

   public static double rollDistancePerAgilityLevel() {
      ensureLoaded();
      return rollDistancePerAgilityLevel;
   }

   public static double rollRechargePerAgilityLevel() {
      ensureLoaded();
      return rollRechargePerAgilityLevel;
   }

   public static int extraRolls(int var0) {
      ensureLoaded();
      int var1 = 0;

      for (int var5 : extraRollThresholds) {
         if (var0 >= var5) {
            var1++;
         }
      }

      return var1;
   }

   public static double staminaPerEnduranceLevel() {
      return maxStaminaPerEnduranceLevel();
   }

   public static double maxStaminaPerEnduranceLevel() {
      ensureLoaded();
      return maxStaminaPerEnduranceLevel;
   }

   public static double staminaRegenPerEnduranceLevel() {
      ensureLoaded();
      return staminaRegenPerEnduranceLevel;
   }

   public static double foodEffectivenessPerEnduranceLevel() {
      ensureLoaded();
      return foodEffectivenessPerEnduranceLevel;
   }

   public static double fortunePerLuckLevel() {
      ensureLoaded();
      return fortunePerLuckLevel;
   }

   public static double zenithCritChancePerLuckLevel() {
      ensureLoaded();
      return zenithCritChancePerLuckLevel;
   }

   public static double xpGainPerLuckLevel() {
      ensureLoaded();
      return xpGainPerLuckLevel;
   }

   public static double zenithDrawSpeedPerArcheryLevel() {
      ensureLoaded();
      return zenithDrawSpeedPerArcheryLevel;
   }

   public static double currencyGainPerTradeLevel() {
      ensureLoaded();
      return currencyGainPerTradeLevel;
   }

   public static double manaPerAlchemyLevel() {
      return maxManaPerAlchemyLevel();
   }

   public static double maxManaPerAlchemyLevel() {
      ensureLoaded();
      return maxManaPerAlchemyLevel;
   }

   public static double manaRegenPerAlchemyLevel() {
      ensureLoaded();
      return manaRegenPerAlchemyLevel;
   }

   private static String rootMessage(Throwable var0) {
      Throwable var1 = var0;

      while (var1.getCause() != null) {
         var1 = var1.getCause();
      }

      String var2 = var1.getMessage();
      return var1.getClass().getSimpleName() + (var2 == null ? "" : ": " + var2);
   }
}
