package com.pathoftheascended.levelzplus.compat.jobs.mixin;

import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.init.CriteriaInit;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fix JobsAddon 1.1.6 subtracting the NEXT rank's XP cost from the previous
 * rank's XP balance. The original loop may create negative progress.
 */
@Mixin(value = JobsManager.class, remap = false)
public abstract class JobsAddonLevelingMixin {
    @Inject(method = "addJobXP", at = @At("HEAD"), cancellable = true, remap = false)
    private void levelzplus$correctJobXp(PlayerEntity player, String job, int earnedXp, CallbackInfo ci) {
        if (!JobsAddonCompatConfig.fixXpOverflow()) return;

        ci.cancel();
        if (earnedXp <= 0 || player.getWorld().isClient()) return;
        JobsManager manager = (JobsManager) (Object) this;

        if (manager.getJobLevel(job) >= ConfigInit.CONFIG.jobMaxLevel) {
            manager.setJobXP(job, 0);
            return;
        }

        long remaining = Math.max(0L, (long) manager.getJobXP(job)) + earnedXp;
        while (manager.getJobLevel(job) < ConfigInit.CONFIG.jobMaxLevel) {
            // Read the cost BEFORE increasing the level.
            int cost = Math.max(1, manager.getNextJobLevelExperience(job));
            if (remaining < cost) break;
            remaining -= cost;

            int before = manager.getJobLevel(job);
            manager.addJobExperienceLevels(player, job, 1);
            if (manager.getJobLevel(job) <= before) break;

            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_LEVELUP, player.getSoundCategory(), 1.0F, 1.0F);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                player.getScoreboard().forEachScore(CriteriaInit.JOBS,
                    player.getEntityName(), ScoreboardPlayerScore::incrementScore);
                CriteriaInit.JOB_UP.trigger(serverPlayer, job, manager.getJobLevel(job));
                JobsServerPacket.writeS2CJobPacket(manager, serverPlayer);
            }
        }

        // Do not retain overflow past the configured level cap.
        if (manager.getJobLevel(job) >= ConfigInit.CONFIG.jobMaxLevel) remaining = 0L;
        manager.setJobXP(job, (int) Math.min(Integer.MAX_VALUE, remaining));
    }
}
