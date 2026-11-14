package com.pathoftheascended.levelzplus.compat.jobs;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;

/** JobsAddon's standard Lumberjack rate and per-block overrides, for Zarith cells. */
public final class JobsAddonFelledBridge {
    private JobsAddonFelledBridge() {}

    public static void onHarvest(ServerPlayerEntity player, BlockState state) {
        if (player.isCreative() || !state.isIn(BlockTags.LOGS)) return;
        JobsManager manager = ((JobsManagerAccess) player).getJobsManager();
        if (manager == null || !manager.isEmployedJob("lumberjack")) return;

        int blockId = Registries.BLOCK.getRawId(state.getBlock());
        int xp = JobLists.lumberjackBlockIdMap.getOrDefault(blockId, ConfigInit.CONFIG.lumberjackXP);
        if (xp > 0) {
            JobsServerPacket.writeS2CJobXPPacket(player, "lumberjack", xp);
        }
    }
}
