package com.pathoftheascended.levelzplus.compat.jobs.mixin;

import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig;
import net.jobsaddon.jobs.JobsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Block job removal server-side as well as in the client manager. */
@Mixin(value = JobsManager.class, remap = false)
public abstract class JobsAddonJobLockMixin {
    @Inject(method = "quitJob", at = @At("HEAD"), cancellable = true, remap = false)
    private void levelzplus$preventQuit(String jobName, CallbackInfo ci) {
        if (JobsAddonCompatConfig.lockAllJobs()) ci.cancel();
    }
}
