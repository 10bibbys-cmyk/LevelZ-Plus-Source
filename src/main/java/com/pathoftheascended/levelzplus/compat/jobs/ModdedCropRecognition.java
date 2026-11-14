package com.pathoftheascended.levelzplus.compat.jobs;

import java.lang.reflect.Method;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

/**
 * Broad modded crop maturity detection for the native JobsAddon break path.
 *
 * Actually Harvest maintains a registry of exact mature crop states, including
 * modded blocks configured or auto-discovered by Actually Harvest. Consult it
 * when available. Fall back to conventional crop tags and crop age properties,
 * not item-name whitelists or broad food drops.
 *
 * This is used ONLY for modded blocks that native JobsAddon has not rewarded.
 * Normal right-click XP is handled by ActuallyHarvestJobMixin separately.
 */
public final class ModdedCropRecognition {
    private static final TagKey<net.minecraft.block.Block> MINECRAFT_CROPS =
        TagKey.of(RegistryKeys.BLOCK, new Identifier("minecraft", "crops"));
    private static final TagKey<net.minecraft.block.Block> COMMON_CROPS =
        TagKey.of(RegistryKeys.BLOCK, new Identifier("c", "crops"));
    private static final TagKey<net.minecraft.block.Block> EXPLICIT_CROPS =
        TagKey.of(RegistryKeys.BLOCK, new Identifier("levelz_plus", "farmer_extra_crops"));

    private static volatile Method actuallyHarvestCropsGetter;
    private static volatile boolean getterAttempted;

    private ModdedCropRecognition() {}

    public static boolean isMatureModdedCrop(BlockState state) {
        if (state == null || state.isAir()) return false;
        Identifier blockId = Registries.BLOCK.getId(state.getBlock());
        if ("minecraft".equals(blockId.getNamespace())) return false;

        // Actually Harvest maps mature BlockStates to the replanted states.
        // This works for all mods registered in its crop registry, including
        // non-CropBlock subclasses and crops with custom age properties.
        if (isRegisteredMatureCrop(state)) return true;

        // For crops not supported by Actually Harvest, fall back to normal
        // crop tags or CropBlock inheritance; age must be at its maximum.
        if (!(state.getBlock() instanceof CropBlock)
            && !state.isIn(MINECRAFT_CROPS)
            && !state.isIn(COMMON_CROPS)
            && !state.isIn(EXPLICIT_CROPS)) return false;

        for (Property<?> property : state.getProperties()) {
            if (!(property instanceof IntProperty age)) continue;
            String name = age.getName();
            if (!name.equals("age") && !name.equals("growth")
                && !name.equals("stage") && !name.equals("crop_age")) continue;
            int current = state.get(age);
            int max = age.getValues().stream().mapToInt(Integer::intValue).max().orElse(-1);
            return max >= 0 && current >= max;
        }
        // Explicitly tagged non-aging plants (e.g. naturally grown wild
        // Farmer's Delight crops) are harvestable at any state. Restrict this
        // to curated data entries so it can't reward arbitrary decorations.
        return state.isIn(EXPLICIT_CROPS);
    }

    private static boolean isRegisteredMatureCrop(BlockState state) {
        if (!FabricLoader.getInstance().isModLoaded("actuallyharvest")) return false;
        Method getter = actuallyHarvestCropsGetter;
        if (!getterAttempted) {
            synchronized (ModdedCropRecognition.class) {
                if (!getterAttempted) {
                    try {
                        getter = Class.forName("actuallyharvest.config.ConfigHandler$Common")
                            .getMethod("getCrops");
                        actuallyHarvestCropsGetter = getter;
                    } catch (ReflectiveOperationException error) {
                        System.err.println("[LevelZ+] [JobsAddon] Actually Harvest mature-crop registry unavailable: "
                            + error.getMessage());
                    }
                    getterAttempted = true;
                }
            }
        }
        getter = actuallyHarvestCropsGetter;
        if (getter == null) return false;
        try {
            Object crops = getter.invoke(null);
            return crops instanceof Map<?, ?> map && map.containsKey(state);
        } catch (ReflectiveOperationException error) {
            return false;
        }
    }
}
