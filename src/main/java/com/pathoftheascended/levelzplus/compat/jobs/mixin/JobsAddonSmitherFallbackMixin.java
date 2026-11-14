package com.pathoftheascended.levelzplus.compat.jobs.mixin;

import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig;
import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonSmitherBridge;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.jobs.JobHelper;
import net.jobsaddon.jobs.JobsManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fallback for modded weapons not subclassing ToolItem/ArmorItem.
 * Compare job progress to avoid paying twice for native-recognised gear.
 */
@Mixin(value = JobHelper.class, remap = false)
public abstract class JobsAddonSmitherFallbackMixin {
    @Unique private static final ThreadLocal<int[]> levelzplus$before = new ThreadLocal<>();

    @Inject(method = "addSmitherXp", at = @At("HEAD"), remap = false)
    private static void levelzplus$beforeSmither(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        levelzplus$before.remove();
        if (!(player instanceof ServerPlayerEntity server)
            || !JobsAddonCompatConfig.expandedGearSmitherBridge()) return;
        JobsManager jobs = ((JobsManagerAccess) server).getJobsManager();
        if (jobs != null) levelzplus$before.set(new int[] {
            jobs.getJobLevel("smither"), jobs.getJobXP("smither")});
    }

    @Inject(method = "addSmitherXp", at = @At("TAIL"), remap = false)
    private static void levelzplus$afterSmither(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        int[] before = levelzplus$before.get();
        levelzplus$before.remove();
        if (before == null || !(player instanceof ServerPlayerEntity server)) return;
        JobsManager jobs = ((JobsManagerAccess) server).getJobsManager();
        if (jobs != null && jobs.getJobLevel("smither") == before[0]
            && jobs.getJobXP("smither") == before[1]) {
            JobsAddonSmitherBridge.award(server, stack);
        }
    }
}
