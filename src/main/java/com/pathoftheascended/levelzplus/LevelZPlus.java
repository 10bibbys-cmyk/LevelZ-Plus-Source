package com.pathoftheascended.levelzplus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class LevelZPlus implements ModInitializer {
   public static final String MOD_ID = "levelz_plus";

   @Override
   public void onInitialize() {
      CompatConfig.load();
      ProgressionXpConfig.load();
      com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig.load();
      if (FabricLoader.getInstance().isModLoaded("jobsaddon")
         && com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig.enabled()) {
         com.pathoftheascended.levelzplus.compat.jobs.JobsAddonIntegration.init();
      }
      System.out.println("[LevelZ+] " + version() + " loaded. LevelZ is required; Tom's Storage, Better Combat, Spell Engine, Small Ships, Actually Harvest and JobsAddon adapters are optional.");
      ProgressionXpConfig.validateLevelZSettings();
      if (CompatConfig.debug()) {
         System.out.println("[LevelZ+] Debug logging is ENABLED.");
         reportModule("Tom's Storage", "toms_storage", CompatConfig.moduleTomsStorage());
         reportModule("Better Combat", "bettercombat", CompatConfig.moduleBetterCombat());
         reportModule("Spell Engine", "spell_engine", CompatConfig.moduleSpellEngine());
         reportModule("Small Ships", "smallships", CompatConfig.moduleSmallShips());
         reportModule("Actually Harvest", "actuallyharvest", ProgressionXpConfig.moduleActuallyHarvest());
      }
   }

   private static String version() {
      return FabricLoader.getInstance()
         .getModContainer(MOD_ID)
         .map(container -> container.getMetadata().getVersion().getFriendlyString())
         .orElse("unknown");
   }

   private static void reportModule(String name, String modId, boolean enabled) {
      boolean installed = FabricLoader.getInstance().isModLoaded(modId);
      System.out.println("[LevelZ+] [Modules] " + name + ": configured=" + enabled + ", installed=" + installed + ", active=" + (enabled && installed));
   }
}
