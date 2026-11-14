package com.pathoftheascended.levelzplus.compat.jobs;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Server-side JobsAddon adapter. Only class-load this after checking the
 * optional jobsaddon mod id.
 */
public final class JobsAddonIntegration {
    // These are the internal IDs from JobsAddon 1.1.6. "smither" is intentional.
    private static final String[] ALL_JOBS = {
        "brewer", "builder", "farmer", "fisher",
        "lumberjack", "miner", "smither", "warrior"
    };

    private JobsAddonIntegration() {}

    public static void init() {
        if (!JobsAddonCompatConfig.enabled()) return;
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            server.execute(() -> {
                if (!JobsAddonCompatConfig.autoEmployAll() && !JobsAddonCompatConfig.lockAllJobs()) return;

                ServerPlayerEntity player = handler.player;
                if (player.isRemoved()) return;
                JobsManager manager = ((JobsManagerAccess) player).getJobsManager();
                if (manager == null) return;

                // This setting is intrinsic to auto-enrolling all eight jobs.
                // Users may disable auto-enrolment in LevelZ+ to retain their
                // independently configured JobsAddon job limit.
                ConfigInit.CONFIG.employedJobs = ALL_JOBS.length;
                ConfigInit.CONFIG.jobChangeTime = 0;
                manager.setEmployedJobTime(0);

                boolean changed = false;
                for (String id : ALL_JOBS) {
                    if (!manager.isEmployedJob(id)) {
                        manager.employJob(id);
                        changed = true;
                    }
                }
                // Always sync the server-authoritative employment list, including on reconnect.
                JobsServerPacket.writeS2CJobPacket(manager, player);
                // Sync the effective job count and cooldown to the client.
                JobsServerPacket.writeS2CJobConfigSyncPacket(player, ConfigInit.CONFIG.getJobConfigList());
            })
        );
    }
}
