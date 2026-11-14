package com.pathoftheascended.levelzplus.compat.jobs;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Optional JobsAddon configuration. These keys live in the existing LevelZ+
 * properties file, but only take effect when JobsAddon is installed.
 */
public final class JobsAddonCompatConfig {
    private static volatile boolean loaded;
    private static boolean enabled = false;
    private static boolean autoEmployAll = false;
    private static boolean lockAllJobs = false;
    private static boolean tideFisherBridge = false;
    private static boolean builderPlacementBridge = false;
    private static boolean debugBuilder = false;
    private static boolean runicAttunementBrewerBridge = false;
    private static boolean debugRunicAttunement = false;
    private static boolean customJobsScreenHeader = false;
    private static boolean farmersDelightCookingBridge = false;
    private static boolean moddedCropFarmerBridge = false;
    private static boolean debugFarmersDelightCooking = false;
    private static boolean expandedGearSmitherBridge = false;
    private static boolean fixXpOverflow = false;
    private static boolean actuallyHarvestBridge = false;
    private static boolean debugActuallyHarvest = false;
    private static boolean felledLumberjackBridge = false;
    private static boolean protectFelledLogs = false;

    private JobsAddonCompatConfig() {}

    public static synchronized void load() {
        if (loaded) return;
        // Mixins may load before ModInitializer; ensure the main config exists.
        com.pathoftheascended.levelzplus.CompatConfig.load();
        loaded = true;

        Path path = FabricLoader.getInstance().getConfigDir().resolve("levelz_plus.properties");
        Properties properties = new Properties();
        try {
            if (Files.isRegularFile(path)) {
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }

                Map<String, String> defaults = new LinkedHashMap<>();
                defaults.put("jobsaddon.enabled", "false");
                defaults.put("jobsaddon.auto_employ_all", "false");
                defaults.put("jobsaddon.lock_all_jobs", "false");
                defaults.put("jobsaddon.tide_fisher_bridge", "false");
                defaults.put("jobsaddon.builder_placement_bridge", "false");
                defaults.put("jobsaddon.debug_builder", "false");
                defaults.put("jobsaddon.runic_attunement_brewer_bridge", "false");
                defaults.put("jobsaddon.debug_runic_attunement", "false");
                defaults.put("jobsaddon.custom_jobs_screen_header", "false");
                defaults.put("jobsaddon.farmers_delight_cooking_bridge", "false");
                defaults.put("jobsaddon.modded_crop_farmer_bridge", "false");
                defaults.put("jobsaddon.debug_farmers_delight_cooking", "false");
                defaults.put("jobsaddon.expanded_gear_smither_bridge", "false");
                defaults.put("jobsaddon.fix_xp_overflow", "false");
                // Migrate off TEST2's persisted false default without
                // overriding unrelated user configuration.
                defaults.put("jobsaddon.actually_harvest_bridge_v2", "false");
                defaults.put("jobsaddon.debug_actually_harvest", "false");
                defaults.put("jobsaddon.felled_lumberjack_bridge", "false");
                defaults.put("felled.protect_fallen_logs", "false");

                StringBuilder missing = new StringBuilder();
                for (var entry : defaults.entrySet()) {
                    if (!properties.containsKey(entry.getKey())) {
                        missing.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
                        properties.setProperty(entry.getKey(), entry.getValue());
                    }
                }
                if (missing.length() != 0) {
                    Files.writeString(path,
                        "\n# -------------------- Optional JobsAddon 1.1.6 integration --------------------\n"
                        + "# JobsAddon remains optional. All compatibility options default to FALSE.\n"
                        + "# Enable jobsaddon.enabled=true AND the desired feature toggles.\n"
                        + "# Server restart required after edits. Separate client config must match UI toggles.\n"
                        + "# auto_employ_all grants all eight professions on login.\n"
                        + "# lock_all_jobs prevents quitting (also enforces all eight jobs).\n"
                        + "# custom_jobs_screen_header replaces employment/cooldown labels when jobs are locked.\n"
                        + "# builder_placement_bridge fixes missed block-place XP.\n"
                        + "# runic_attunement_brewer_bridge awards Brewer XP after successful attunement.\n"
                        + "# actually_harvest_bridge_v2 fixes right-click harvesting; TEST2 key is obsolete.\n"
                        + "# felled.protect_fallen_logs is independent, and OFF unless explicitly enabled.\n"
                        + missing,
                        StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                }
            }
        } catch (IOException error) {
            System.err.println("[LevelZ+] [JobsAddon] Could not read/write LevelZ+ jobs configuration: "
                + error.getMessage());
        }

        enabled = readBoolean(properties, "jobsaddon.enabled", enabled);
        autoEmployAll = readBoolean(properties, "jobsaddon.auto_employ_all", autoEmployAll);
        lockAllJobs = readBoolean(properties, "jobsaddon.lock_all_jobs", lockAllJobs);
        tideFisherBridge = readBoolean(properties, "jobsaddon.tide_fisher_bridge", tideFisherBridge);
        builderPlacementBridge = readBoolean(properties, "jobsaddon.builder_placement_bridge", builderPlacementBridge);
        debugBuilder = readBoolean(properties, "jobsaddon.debug_builder", debugBuilder);
        runicAttunementBrewerBridge = readBoolean(properties, "jobsaddon.runic_attunement_brewer_bridge", runicAttunementBrewerBridge);
        debugRunicAttunement = readBoolean(properties, "jobsaddon.debug_runic_attunement", debugRunicAttunement);
        customJobsScreenHeader = readBoolean(properties, "jobsaddon.custom_jobs_screen_header", customJobsScreenHeader);
        farmersDelightCookingBridge = readBoolean(properties, "jobsaddon.farmers_delight_cooking_bridge", farmersDelightCookingBridge);
        moddedCropFarmerBridge = readBoolean(properties, "jobsaddon.modded_crop_farmer_bridge", moddedCropFarmerBridge);
        debugFarmersDelightCooking = readBoolean(properties, "jobsaddon.debug_farmers_delight_cooking", debugFarmersDelightCooking);
        expandedGearSmitherBridge = readBoolean(properties, "jobsaddon.expanded_gear_smither_bridge", expandedGearSmitherBridge);
        fixXpOverflow = readBoolean(properties, "jobsaddon.fix_xp_overflow", fixXpOverflow);
        actuallyHarvestBridge = readBoolean(properties, "jobsaddon.actually_harvest_bridge_v2", actuallyHarvestBridge);
        debugActuallyHarvest = readBoolean(properties, "jobsaddon.debug_actually_harvest", debugActuallyHarvest);
        felledLumberjackBridge = readBoolean(properties, "jobsaddon.felled_lumberjack_bridge", felledLumberjackBridge);
        protectFelledLogs = readBoolean(properties, "felled.protect_fallen_logs", protectFelledLogs);
    }

    private static boolean readBoolean(Properties values, String key, boolean fallback) {
        String raw = values.getProperty(key);
        if (raw == null) return fallback;
        if ("true".equalsIgnoreCase(raw.trim())) return true;
        if ("false".equalsIgnoreCase(raw.trim())) return false;
        System.err.println("[LevelZ+] [JobsAddon] Invalid Boolean " + key + "=" + raw);
        return fallback;
    }

    public static boolean enabled() { load(); return enabled; }
    public static boolean autoEmployAll() { load(); return autoEmployAll; }
    public static boolean lockAllJobs() { load(); return lockAllJobs; }
    public static boolean tideFisherBridge() { load(); return tideFisherBridge; }
    public static boolean builderPlacementBridge() { load(); return builderPlacementBridge; }
    public static boolean debugBuilder() { load(); return debugBuilder; }
    public static boolean runicAttunementBrewerBridge() { load(); return runicAttunementBrewerBridge; }
    public static boolean debugRunicAttunement() { load(); return debugRunicAttunement; }
    public static boolean customJobsScreenHeader() { load(); return customJobsScreenHeader; }
    public static boolean farmersDelightCookingBridge() { load(); return farmersDelightCookingBridge; }
    public static boolean moddedCropFarmerBridge() { load(); return moddedCropFarmerBridge; }
    public static boolean debugFarmersDelightCooking() { load(); return debugFarmersDelightCooking; }
    public static boolean expandedGearSmitherBridge() { load(); return expandedGearSmitherBridge; }
    public static boolean fixXpOverflow() { load(); return fixXpOverflow; }
    public static boolean actuallyHarvestBridge() { load(); return actuallyHarvestBridge; }
    public static boolean debugActuallyHarvest() { load(); return debugActuallyHarvest; }
    public static boolean felledLumberjackBridge() { load(); return felledLumberjackBridge; }
    public static boolean protectFelledLogs() { load(); return protectFelledLogs; }
}
