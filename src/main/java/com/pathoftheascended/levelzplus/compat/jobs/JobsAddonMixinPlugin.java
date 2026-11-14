package com.pathoftheascended.levelzplus.compat.jobs;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class JobsAddonMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String packageName) {
        JobsAddonCompatConfig.load();
    }

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public boolean shouldApplyMixin(String targetName, String mixinName) {
        if (!FabricLoader.getInstance().isModLoaded("jobsaddon") || !JobsAddonCompatConfig.enabled()) {
            return false;
        }
        if (mixinName.endsWith("JobsAddonJobLockMixin") || mixinName.endsWith("JobsAddonJobScreenMixin")) {
            return JobsAddonCompatConfig.lockAllJobs();
        }
        if (mixinName.endsWith("FarmersDelightPotJobMixin") || mixinName.endsWith("FarmersDelightSkilletJobMixin")) {
            return FabricLoader.getInstance().isModLoaded("farmersdelight") && JobsAddonCompatConfig.farmersDelightCookingBridge();
        }
        if (mixinName.endsWith("JobsAddonModdedCropBreakMixin")) {
            return JobsAddonCompatConfig.moddedCropFarmerBridge();
        }
        if (mixinName.endsWith("JobsAddonBuilderPlacementMixin")) {
            return JobsAddonCompatConfig.builderPlacementBridge();
        }
        if (mixinName.endsWith("RunicAttunementBrewerJobMixin")) {
            return FabricLoader.getInstance().isModLoaded("runic_attunement")
                && JobsAddonCompatConfig.runicAttunementBrewerBridge();
        }
        if (mixinName.endsWith("TideHookRetrieveMixin")) {
            return FabricLoader.getInstance().isModLoaded("tide") && JobsAddonCompatConfig.tideFisherBridge();
        }
        if (mixinName.endsWith("JobsAddonSmitherFallbackMixin")) {
            return JobsAddonCompatConfig.expandedGearSmitherBridge();
        }
        if (mixinName.endsWith("JobsAddonLevelingMixin")) {
            return JobsAddonCompatConfig.fixXpOverflow();
        }
        if (mixinName.endsWith("ActuallyHarvestJobMixin")) {
            return FabricLoader.getInstance().isModLoaded("actuallyharvest")
                && JobsAddonCompatConfig.actuallyHarvestBridge();
        }
        return false;
    }

    @Override public void acceptTargets(Set<String> mine, Set<String> others) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String name, ClassNode node, String mixin, IMixinInfo info) {}
    @Override public void postApply(String name, ClassNode node, String mixin, IMixinInfo info) {}
}
