package com.pathoftheascended.levelzplus.compat.jobs.mixin;

import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig;
import com.pathoftheascended.levelzplus.compat.jobs.ModdedCropRecognition;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.JobHelper;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * JobsAddon handles native crops only if their block is a PlantBlock and
 * their drops are in jobsaddon:farmer_breaking_items. Many modded crops
 * (including some Farmer's Delight variants) fail one of those tests.
 *
 * JobHelper.addFarmerBlockDropXp is called for normal block-breaking in
 * JobsAddon's BlockMixin. Keep its existing native awards intact and grant
 * a base Farmer reward for mature modded crops it did NOT credit.
 *
 * Actually Harvest right-click actions don't call this normal break method
 * and have their own separately validated bridge.
 */
@Mixin(value = JobHelper.class, remap = false)
public abstract class JobsAddonModdedCropBreakMixin {
    @Unique private static final ThreadLocal<Snapshot> levelzplus$moddedCrop =
        new ThreadLocal<>();

    @Unique
    private record Snapshot(ServerPlayerEntity player, BlockPos pos, BlockState state,
                            int beforeLevel, int beforeXP) {}

    @Inject(method = "addFarmerBlockDropXp", at = @At("HEAD"), remap = false, require = 1)
    private static void levelzplus$beforeNativeFarmerBreak(PlayerEntity player,
            BlockPos pos, BlockState state, CallbackInfo ci) {
        levelzplus$moddedCrop.remove();
        if (!JobsAddonCompatConfig.enabled()
            || !JobsAddonCompatConfig.moddedCropFarmerBridge()
            || !(player instanceof ServerPlayerEntity server) || server.isCreative()
            || !ModdedCropRecognition.isMatureModdedCrop(state)) return;

        JobsManager manager = ((JobsManagerAccess) server).getJobsManager();
        if (manager == null || !manager.isEmployedJob("farmer")
            || manager.isJobMaxLevel("farmer")) return;
        levelzplus$moddedCrop.set(new Snapshot(server, pos.toImmutable(), state,
            manager.getJobLevel("farmer"), manager.getJobXP("farmer")));
    }

    @Inject(method = "addFarmerBlockDropXp", at = @At("TAIL"), remap = false, require = 1)
    private static void levelzplus$afterNativeFarmerBreak(PlayerEntity player,
            BlockPos pos, BlockState state, CallbackInfo ci) {
        Snapshot snapshot = levelzplus$moddedCrop.get();
        levelzplus$moddedCrop.remove();
        if (snapshot == null) return;

        JobsManager manager = ((JobsManagerAccess) snapshot.player()).getJobsManager();
        if (manager == null || manager.isJobMaxLevel("farmer")) return;

        // JobsAddon has already rewarded this crop. No fallback necessary.
        if (manager.getJobLevel("farmer") != snapshot.beforeLevel()
            || manager.getJobXP("farmer") != snapshot.beforeXP()) return;

        // One XP award per mature crop block, never multiplied by stack yield.
        int xp = Math.max(0, ConfigInit.CONFIG.farmerXP);
        if (xp > 0) {
            JobsServerPacket.writeS2CJobXPPacket(snapshot.player(), "farmer", xp);
        }
    }
}
