package com.pathoftheascended.levelzplus.compat.jobs.mixin;

import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig;
import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonFisherBridge;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tide 2 records each legitimate captured journal fish via tryLogCatch.
 * This happens on successful retrieval, never on cast/reel with no catch.
 */
@Pseudo
@Mixin(targets = "com.li64.tide.util.TideUtils", remap = false)
public abstract class TideFishJobMixin {
    @Inject(method = "tryLogCatch", at = @At("RETURN"), remap = false, require = 0)
    private static void levelzplus$onSuccessfulTideCatch(ItemStack fish,
        ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (JobsAddonCompatConfig.tideFisherBridge() && Boolean.TRUE.equals(cir.getReturnValue())) {
            JobsAddonFisherBridge.awardFish(player, fish);
        }
    }
}
