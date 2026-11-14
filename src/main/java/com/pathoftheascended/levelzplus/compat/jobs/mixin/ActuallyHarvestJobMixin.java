package com.pathoftheascended.levelzplus.compat.jobs.mixin;

import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonActuallyHarvestBridge;
import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.jobs.JobsManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Actually Harvest 1.1.4.0 Fabric:
 * tryHarvest(World, BlockPos, LivingEntity, Hand, boolean): boolean.
 *
 * A hoe may trigger HARVEST (destroy/replant) OR CLICK (special-crop use).
 * The common completion method covers individual and area hoe harvesting.
 * Award after a successful server-side action changing a crop or its
 * adjacent segment, as happens with multi-block modded crops.
 * Do not use a saved job XP snapshot to suppress these rewards.
 */
@Mixin(targets = "actuallyharvest.event.HarvestEventHandler", remap = false)
public abstract class ActuallyHarvestJobMixin {
    @Unique private static final ThreadLocal<HarvestSnapshot> levelzplus$harvest =
        new ThreadLocal<>();

    @Unique
    private record HarvestSnapshot(BlockState blockState, BlockState above, BlockState below) {}

    @Inject(method = "tryHarvest", at = @At("HEAD"), require = 1, remap = false)
    private static void levelzplus$beforeTryHarvest(World world, BlockPos pos,
        LivingEntity entity, Hand hand, boolean area,
        CallbackInfoReturnable<Boolean> cir) {
        levelzplus$harvest.remove();
        if (!JobsAddonCompatConfig.actuallyHarvestBridge() || world.isClient()
            || !(entity instanceof ServerPlayerEntity player)) return;

        JobsManager jobs = ((JobsManagerAccess) player).getJobsManager();
        if (jobs == null || !jobs.isEmployedJob("farmer")) return;
        // Multi-block plants may replace their upper or lower segment while
        // the clicked block itself stays unchanged.
        levelzplus$harvest.set(new HarvestSnapshot(world.getBlockState(pos),
            world.getBlockState(pos.up()), world.getBlockState(pos.down())));
    }

    @Inject(method = "tryHarvest", at = @At("RETURN"), require = 1, remap = false)
    private static void levelzplus$afterTryHarvest(World world, BlockPos pos,
        LivingEntity entity, Hand hand, boolean area,
        CallbackInfoReturnable<Boolean> cir) {
        HarvestSnapshot original = levelzplus$harvest.get();
        levelzplus$harvest.remove();
        if (world.isClient()) return;

        boolean success = Boolean.TRUE.equals(cir.getReturnValue());
        if (original == null || !(entity instanceof ServerPlayerEntity player)) {
            if (JobsAddonCompatConfig.debugActuallyHarvest() && success) {
                System.out.println("[LevelZ+] [ActuallyHarvest] SUCCESS but no Farmer snapshot at " + pos);
            }
            return;
        }

        BlockState now = world.getBlockState(pos);
        boolean changed = !now.equals(original.blockState())
            || !world.getBlockState(pos.up()).equals(original.above())
            || !world.getBlockState(pos.down()).equals(original.below());
        int earned = 0;

        if (success && changed) {
            // The TEST5 snapshot comparison suppressed verified crop harvests.
            // JobsAddon uses a separate network XP packet; a changed manager
            // value does not prove this particular harvest was rewarded.
            // Only a successful, state-changing server-side harvest qualifies.
            earned = JobsAddonActuallyHarvestBridge.onSuccessfulHarvest(
                player, pos, original.blockState());
        }
        if (JobsAddonCompatConfig.debugActuallyHarvest()) {
            System.out.println("[LevelZ+] [ActuallyHarvest] pos=" + pos +
                " success=" + success + " blockChanged=" + changed +
                " crop=" + original.blockState().getBlock() +
                " awardedFarmerXP=" + earned);
        }
    }
}
