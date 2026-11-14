package com.pathoftheascended.levelzplus.compat.felled;

import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Keep the Zarith and Felled targets fully optional. */
public final class FelledMixinPlugin implements IMixinConfigPlugin {
    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetName, String mixinClassName) {
        if (!FabricLoader.getInstance().isModLoaded("felled")
            || !FabricLoader.getInstance().isModLoaded("zarith")) return false;
        // Default public install must not change Felled's mechanics or
        // apply block-cell hooks unless a specific optional feature is on.
        return JobsAddonCompatConfig.protectFelledLogs()
            || (FabricLoader.getInstance().isModLoaded("jobsaddon")
                && JobsAddonCompatConfig.enabled()
                && JobsAddonCompatConfig.felledLumberjackBridge());
    }
    @Override public void acceptTargets(Set<String> ours, Set<String> others) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    @Override public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
