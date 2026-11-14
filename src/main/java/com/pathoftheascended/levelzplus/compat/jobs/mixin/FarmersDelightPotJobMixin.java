package com.pathoftheascended.levelzplus.compat.jobs.mixin;

import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.data.JobLists;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Farmer's Delight Refabricated 2.4.1, Minecraft Fabric 1.20.1.
 *
 * CookingPotResultSlot.method_7667(PlayerEntity, ItemStack) is the actual
 * result-slot onTake callback, used for normal takes and successful quick moves.
 * The previous method_7669 hook demanded removeCount > 0, which can be zero
 * during normal result collection. Stop depending on that counter entirely.
 *
 * Capture JobsAddon progress at the start of this result collection; after
 * Farmer's Delight's original handler runs, award only when native JobsAddon
 * did not already award the same crafting action.
 */
@Pseudo
@Mixin(targets = "vectorwing.farmersdelight.common.block.entity.container.CookingPotResultSlot", remap = false)
public abstract class FarmersDelightPotJobMixin {
    @Unique private PotTakeSnapshot levelzplus$potTake;

    @Unique
    private record PotTakeSnapshot(ServerPlayerEntity player, ItemStack output,
                                   int farmerLevel, int farmerXP) {}

    @Inject(method = "method_7667", at = @At("HEAD"), remap = false, require = 1)
    private void levelzplus$beforePotTake(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        levelzplus$potTake = null;
        if (!JobsAddonCompatConfig.farmersDelightCookingBridge()
            || !(player instanceof ServerPlayerEntity serverPlayer)) return;

        JobsManager jobs = ((JobsManagerAccess) serverPlayer).getJobsManager();
        if (jobs == null || !jobs.isEmployedJob("farmer")) return;

        // Quick-moving the entire result may pass an already emptied stack
        // to onTake. An empty stack here DOES NOT mean no food was collected.
        ItemStack output = stack == null ? ItemStack.EMPTY : stack.copy();
        levelzplus$potTake = new PotTakeSnapshot(serverPlayer, output,
            jobs.getJobLevel("farmer"), jobs.getJobXP("farmer"));
    }

    @Inject(method = "method_7667", at = @At("TAIL"), remap = false, require = 1)
    private void levelzplus$afterPotTake(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        PotTakeSnapshot snapshot = levelzplus$potTake;
        levelzplus$potTake = null;
        if (snapshot == null) return;

        JobsManager jobs = ((JobsManagerAccess) snapshot.player()).getJobsManager();
        if (jobs == null || !jobs.isEmployedJob("farmer")) return;

        // Item.onCraft can award native JobsAddon Farmer XP in some paths.
        // Do not double-credit those takes.
        boolean nativeAwarded = jobs.getJobLevel("farmer") != snapshot.farmerLevel()
            || jobs.getJobXP("farmer") != snapshot.farmerXP();
        int earned = 0;
        if (!nativeAwarded && !jobs.isJobMaxLevel("farmer")) {
            ItemStack output = snapshot.output();
            int count = output.isEmpty() ? 1 : Math.min(64, output.getCount());
            int perResult = ConfigInit.CONFIG.farmerXP;
            if (!output.isEmpty()) {
                int rawId = Registries.ITEM.getRawId(output.getItem());
                perResult = JobLists.farmerCraftingIdMap.getOrDefault(rawId, perResult);
            }
            earned = (int) Math.max(0L, Math.min(256L, (long) perResult * count));
            if (earned > 0) {
                JobsServerPacket.writeS2CJobXPPacket(snapshot.player(), "farmer", earned);
            }
        }

        if (JobsAddonCompatConfig.debugFarmersDelightCooking()) {
            System.out.println("[LevelZ+] [FarmersDelight] Cooking Pot result collected:" +
                " output=" + snapshot.output() +
                " nativeAwarded=" + nativeAwarded +
                " awardedFarmerXP=" + earned +
                " farmerLevel=" + snapshot.farmerLevel() + "->" +
                jobs.getJobLevel("farmer") +
                " farmerProgress=" + snapshot.farmerXP() + "->" +
                jobs.getJobXP("farmer"));
        }
    }
}
