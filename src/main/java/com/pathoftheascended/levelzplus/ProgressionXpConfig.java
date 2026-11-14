package com.pathoftheascended.levelzplus;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;
import net.levelz.init.ConfigInit;

public final class ProgressionXpConfig {
   private static volatile boolean loaded;

   private static boolean moduleActuallyHarvest = true;

   private static boolean combatEnabled = false;
   private static boolean combatFailSafe = false;
   private static double rewardPercentPerContributor = 100.0;
   private static double combatMobXpMultiplier = 1.0;
   private static double combatDragonXpMultiplier = 0.5;
   private static double combatMaxHealthFraction = 0.10;

   private static boolean actuallyHarvestEnabled = true;
   private static double actuallyHarvestXpMultiplier = 1.0;

   // Fallback values for Actually Harvest settings when its config is absent.
   private static boolean harvestUseRange = true;
   private static int harvestRangeMin = 1;
   private static int harvestRangeMax = 4;
   private static int harvestChance = 100;
   private static int harvestAmount = 2;

   private ProgressionXpConfig() {
   }

   public static synchronized void load() {
      if (loaded) {
         return;
      }
      loaded = true;

      Path configDir = FabricLoader.getInstance().getConfigDir();
      loadLevelZPlusProperties(configDir.resolve("levelz_plus.properties"));
      loadActuallyHarvestConfig(configDir.resolve("actuallyharvest-common.toml"));
   }

   private static void loadLevelZPlusProperties(Path config) {
      if (!Files.exists(config)) {
         return;
      }

      Properties properties = new Properties();
      try (BufferedReader reader = Files.newBufferedReader(config, StandardCharsets.UTF_8)) {
         properties.load(reader);
         moduleActuallyHarvest = readBoolean(properties, "modules.actually_harvest", moduleActuallyHarvest);
         combatEnabled = readBoolean(properties, "levelz_xp.combat.enabled", combatEnabled);
         combatFailSafe = readBoolean(properties, "levelz_xp.combat.fail_safe_enabled", combatFailSafe);
         rewardPercentPerContributor = readDouble(properties, "levelz_xp.combat.reward_percent_per_contributor", rewardPercentPerContributor, 0.0, 1000.0);
         combatMobXpMultiplier = readDouble(properties, "levelz_xp.combat.mob_xp_multiplier", combatMobXpMultiplier, 0.0, 1000.0);
         combatDragonXpMultiplier = readDouble(properties, "levelz_xp.combat.dragon_xp_multiplier", combatDragonXpMultiplier, 0.0, 1000.0);
         combatMaxHealthFraction = readDouble(properties, "levelz_xp.combat.max_health_fraction", combatMaxHealthFraction, 0.0, 1000.0);
         actuallyHarvestEnabled = readBoolean(properties, "levelz_xp.actually_harvest.enabled", actuallyHarvestEnabled);
         actuallyHarvestXpMultiplier = readDouble(properties, "levelz_xp.actually_harvest.multiplier", actuallyHarvestXpMultiplier, 0.0, 1000.0);
      } catch (Exception error) {
         System.err.println("[LevelZ+] Failed to load progression XP settings; using safe defaults. "
            + error.getClass().getSimpleName() + ": " + error.getMessage());
      }
   }

   private static void loadActuallyHarvestConfig(Path config) {
      if (!Files.exists(config)) {
         return;
      }

      try {
         String text = Files.readString(config, StandardCharsets.UTF_8);
         harvestUseRange = tomlBoolean(text, "xpFromHarvestUseRange", harvestUseRange);
         harvestChance = tomlInt(text, "xpFromHarvestChance", harvestChance, 0, 100);
         harvestAmount = tomlInt(text, "xpFromHarvestAmount", harvestAmount, 0, 10);

         Matcher range = Pattern.compile("(?m)^\\s*xpFromHarvestRangeAmount\\s*=\\s*\"(\\d+)\\s*-\\s*(\\d+)\"\\s*$").matcher(text);
         if (range.find()) {
            int min = Integer.parseInt(range.group(1));
            int max = Integer.parseInt(range.group(2));
            if (min >= 0 && max >= min) {
               harvestRangeMin = min;
               harvestRangeMax = max;
            }
         }
      } catch (Exception error) {
         System.err.println("[LevelZ+] [ActuallyHarvest] Could not read actuallyharvest-common.toml; "
            + "using built-in harvest XP defaults. " + error.getClass().getSimpleName() + ": " + error.getMessage());
      }
   }

   public static void validateLevelZSettings() {
      load();

      boolean effectiveCombat = combatEnabled();
      if ((effectiveCombat || (moduleActuallyHarvest && actuallyHarvestEnabled)) && !ConfigInit.CONFIG.useIndependentExp) {
         System.err.println("[LevelZ+] [ProgressionXP] DISABLED: LevelZ useIndependentExp=false. "
            + "Independent progression XP requires the LevelZ XP pool.");
      }

      if (!combatEnabled && effectiveCombat) {
         System.out.println("[LevelZ+] [CombatXP] FAIL-SAFE: custom combat XP activated because native LevelZ mob XP is zero.");
      } else if (!effectiveCombat && ConfigInit.CONFIG.mobXPMultiplier == 0.0F
         && ConfigInit.CONFIG.useIndependentExp) {
         System.err.println("[LevelZ+] [CombatXP] ERROR: no mob XP route active. Set levelz_xp.combat.enabled=true.");
      }
      if (effectiveCombat && ConfigInit.CONFIG.dragonXPMultiplier > 0.0F && combatDragonXpMultiplier > 0.0) {
         System.err.println("[LevelZ+] [CombatXP] Dragon sharing INACTIVE: native LevelZ dragonXPMultiplier="
            + ConfigInit.CONFIG.dragonXPMultiplier
            + ". Set native dragonXPMultiplier=0.0 and levelz_xp.combat.dragon_xp_multiplier=0.5 to avoid duplicate XP.");
      }
      if (effectiveCombat && ConfigInit.CONFIG.mobXPMultiplier > 0.0F) {
         System.err.println("[LevelZ+] [ProgressionXP] WARNING: LevelZ mobXPMultiplier="
            + ConfigInit.CONFIG.mobXPMultiplier
            + " while LevelZ+ contributor combat XP is enabled. Set LevelZ mobXPMultiplier=0.0 "
            + "to prevent duplicate mob progression XP.");
      }
   }

   public static boolean independentXpReady() {
      return ConfigInit.CONFIG.useIndependentExp;
   }

   public static boolean moduleActuallyHarvest() {
      load();
      return moduleActuallyHarvest;
   }

   public static boolean combatEnabled() {
      load();
      if (combatEnabled) return true;
      if (!combatFailSafe) return false;
      try {
         return ConfigInit.CONFIG != null
            && ConfigInit.CONFIG.useIndependentExp
            && ConfigInit.CONFIG.mobXPMultiplier == 0.0F;
      } catch (RuntimeException | LinkageError ignored) {
         return false;
      }
   }

   public static double rewardFractionPerContributor() {
      load();
      return rewardPercentPerContributor / 100.0;
   }

   public static double combatMobXpMultiplier() {
      load();
      return combatMobXpMultiplier;
   }

   public static double combatDragonXpMultiplier() {
      load();
      return combatDragonXpMultiplier;
   }

   public static double combatMaxHealthFraction() {
      load();
      return combatMaxHealthFraction;
   }

   public static boolean actuallyHarvestEnabled() {
      load();
      return moduleActuallyHarvest && actuallyHarvestEnabled;
   }

   public static double actuallyHarvestXpMultiplier() {
      load();
      return actuallyHarvestXpMultiplier;
   }

   public static int rollActuallyHarvestBaseXp() {
      load();

      if (harvestUseRange) {
         return harvestRangeMin == harvestRangeMax
            ? harvestRangeMin
            : ThreadLocalRandom.current().nextInt(harvestRangeMin, harvestRangeMax + 1);
      }

      if (ThreadLocalRandom.current().nextInt(100) + 1 <= harvestChance) {
         return harvestAmount;
      }
      return 0;
   }

   private static boolean tomlBoolean(String text, String key, boolean fallback) {
      Matcher matcher = Pattern.compile("(?m)^\\s*" + Pattern.quote(key) + "\\s*=\\s*(true|false)\\s*$", Pattern.CASE_INSENSITIVE).matcher(text);
      return matcher.find() ? Boolean.parseBoolean(matcher.group(1)) : fallback;
   }

   private static int tomlInt(String text, String key, int fallback, int min, int max) {
      Matcher matcher = Pattern.compile("(?m)^\\s*" + Pattern.quote(key) + "\\s*=\\s*(\\d+)\\s*$").matcher(text);
      if (!matcher.find()) {
         return fallback;
      }
      try {
         int value = Integer.parseInt(matcher.group(1));
         return value >= min && value <= max ? value : fallback;
      } catch (NumberFormatException ignored) {
         return fallback;
      }
   }

   private static boolean readBoolean(Properties properties, String key, boolean fallback) {
      String value = properties.getProperty(key);
      if (value == null || value.isBlank()) {
         return fallback;
      }
      if ("true".equalsIgnoreCase(value.trim())) {
         return true;
      }
      if ("false".equalsIgnoreCase(value.trim())) {
         return false;
      }
      System.err.println("[LevelZ+] Invalid boolean config value for '" + key + "': " + value + ". Keeping " + fallback + ".");
      return fallback;
   }

   private static int readInt(Properties properties, String key, int fallback, int min, int max) {
      String value = properties.getProperty(key);
      if (value == null || value.isBlank()) {
         return fallback;
      }
      try {
         int parsed = Integer.parseInt(value.trim());
         if (parsed >= min && parsed <= max) {
            return parsed;
         }
      } catch (NumberFormatException ignored) {
      }
      System.err.println("[LevelZ+] Invalid integer config value for '" + key + "': " + value + ". Keeping " + fallback + ".");
      return fallback;
   }

   private static double readDouble(Properties properties, String key, double fallback, double min, double max) {
      String value = properties.getProperty(key);
      if (value == null || value.isBlank()) {
         return fallback;
      }
      try {
         double parsed = Double.parseDouble(value.trim());
         if (Double.isFinite(parsed) && parsed >= min && parsed <= max) {
            return parsed;
         }
      } catch (NumberFormatException ignored) {
      }
      System.err.println("[LevelZ+] Invalid numeric config value for '" + key + "': " + value + ". Keeping " + fallback + ".");
      return fallback;
   }
}
