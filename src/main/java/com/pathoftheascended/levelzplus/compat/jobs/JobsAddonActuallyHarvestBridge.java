package com.pathoftheascended.levelzplus.compat.jobs;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Farmer XP from Actually Harvest 1.1.4.0's successful tryHarvest path.
 * Executed server-side only for each successfully harvested crop.
 *
 * Unlike normal block breaking, Actually Harvest can replant directly and
 * does not invoke JobsAddon's Block.onBreak mixin. All successfully harvested
 * crops earn the base Farmer XP once, regardless of namespace or drop count.
 */
public final class JobsAddonActuallyHarvestBridge {
    private JobsAddonActuallyHarvestBridge() {}

    public static int onSuccessfulHarvest(ServerPlayerEntity player, BlockPos pos, BlockState original) {
        // PotA grants Actually Harvest profession XP in Survival only.
        // This matches the successfully tested TEST6 behaviour and prevents
        // farming job levels by free Creative-mode harvesting.
        if (player == null || player.isCreative() || original == null
            || original.isAir() || !(player.getWorld() instanceof ServerWorld world)) return 0;

        JobsManager jobs = ((JobsManagerAccess) player).getJobsManager();
        if (jobs == null || !jobs.isEmployedJob("farmer")) {
            if (JobsAddonCompatConfig.debugActuallyHarvest()) {
                System.out.println("[LevelZ+] [ActuallyHarvest] XP rejected: Farmer job not enabled at " + pos);
            }
            return 0;
        }

        // No JobsAddon lastBlockId guard here. Right-click area harvesting
        // can already touch that guard through other block hooks; reusing it
        // rejects legitimate harvests. Successful, state-changing crops are
        // validated by ActuallyHarvestJobMixin before entering this method.

        // One fixed Farmer XP award for every successful crop harvest, no
        // matter which mod owns the crop or how many items its loot table
        // produces. Do not simulate modded loot tables (some require an
        // active block entity/tool/loot context and can give false results).
        int earned = Math.min(100, Math.max(0, ConfigInit.CONFIG.farmerXP));
        if (earned > 0) {
            int xpBefore = jobs.getJobXP("farmer");
            int levelBefore = jobs.getJobLevel("farmer");
            JobsServerPacket.writeS2CJobXPPacket(player, "farmer", earned);
            if (JobsAddonCompatConfig.debugActuallyHarvest()) {
                System.out.println("[LevelZ+] [ActuallyHarvest] Farmer XP sent: mode=" +
                    (player.isCreative() ? "creative" : "non-creative") +
                    " amount=" + earned + " level=" + levelBefore + "->" +
                    jobs.getJobLevel("farmer") + " progress=" + xpBefore + "->" +
                    jobs.getJobXP("farmer") + " maxed=" + jobs.isJobMaxLevel("farmer"));
            }
        } else if (JobsAddonCompatConfig.debugActuallyHarvest()) {
            System.out.println("[LevelZ+] [ActuallyHarvest] XP rejected: base Farmer XP is zero at " + pos);
        }
        return earned;
    }
}
