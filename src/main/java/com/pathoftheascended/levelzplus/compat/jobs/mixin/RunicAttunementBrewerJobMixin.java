package com.pathoftheascended.levelzplus.compat.jobs.mixin;

import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runic Attunement successfully applied an enchantment to an item.
 * RunicInscriptionScreenHandler.tryInscribe calls triggerAttunement only after
 * successful resource validation, enchantment application and material payment.
 *
 * Runs on the server, including Creative test attunements.
 * No XP is granted for opening the table, hovering a tablet, failed attempts,
 * reforge, grind or transmutation.
 */
@Pseudo
@Mixin(targets = "dev.runicattunement.advancement.RunicAdvancements", remap = false)
public abstract class RunicAttunementBrewerJobMixin {
    @Inject(method = "triggerAttunement", at = @At("TAIL"), require = 0, remap = false)
    private static void levelzplus$onSuccessfulAttunement(ServerPlayerEntity player, boolean maxed,
            CallbackInfo ci) {
        if (player == null || player.getWorld().isClient()
            || !JobsAddonCompatConfig.runicAttunementBrewerBridge()) return;

        JobsManager manager = ((JobsManagerAccess) player).getJobsManager();
        if (manager == null || !manager.isEmployedJob("brewer")
            || manager.isJobMaxLevel("brewer")) return;

        int amount = Math.max(0, ConfigInit.CONFIG.brewerXP);
        if (amount > 0) {
            JobsServerPacket.writeS2CJobXPPacket(player, "brewer", amount);
            if (JobsAddonCompatConfig.debugRunicAttunement()) {
                System.out.println("[LevelZ+] [JobsAddon] Runic attunement awarded Brewer XP=" +
                    amount + " creative=" + player.isCreative() +
                    " jobLevel=" + manager.getJobLevel("brewer") +
                    " jobProgress=" + manager.getJobXP("brewer"));
            }
        }
    }
}
