package com.pathoftheascended.levelzplus.compat.jobs;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

/** XP for successful Tide fishing catches, respecting JobsAddon custom rates. */
public final class JobsAddonFisherBridge {
    private JobsAddonFisherBridge() {}

    public static void awardFish(ServerPlayerEntity player, ItemStack caught) {
        if (caught == null || caught.isEmpty() || player.isCreative()) return;
        JobsManager manager = ((JobsManagerAccess) player).getJobsManager();
        if (manager == null || !manager.isEmployedJob("fisher")) return;
        int rawId = Registries.ITEM.getRawId(caught.getItem());
        int perFish = JobLists.fisherItemIdMap.getOrDefault(rawId, ConfigInit.CONFIG.fisherXP);
        long total = (long) perFish * caught.getCount();
        if (total > 0) {
            JobsServerPacket.writeS2CJobXPPacket(player, "fisher",
                (int) Math.min(1000L, total));
        }
    }
}
