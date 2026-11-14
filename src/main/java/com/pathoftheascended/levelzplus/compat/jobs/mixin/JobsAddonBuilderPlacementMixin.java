package com.pathoftheascended.levelzplus.compat.jobs.mixin;

import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.access.PlayerAccess;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.init.TagInit;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * JobsAddon 1.1.6 adds Builder XP from Block.onPlaced, which is not a
 * reliable universal successful-place hook. Here we observe BlockItem.place
 * after success, falling back only when JobsAddon did not already pay XP.
 *
 * Applies in Creative as well as Survival to permit dev testing; JobsAddon's
 * own builder place-repetition limit still applies. Only tagged blocks count.
 */
@Mixin(BlockItem.class)
public abstract class JobsAddonBuilderPlacementMixin {
    @Unique private static final ThreadLocal<Placement> levelzplus$placement =
        new ThreadLocal<>();

    @Unique
    private record Placement(ServerPlayerEntity player, BlockPos pos, BlockState before,
                             int level, int xp) {}

    @Inject(method = "place", at = @At("HEAD"), require = 1)
    private void levelzplus$beforePlace(ItemPlacementContext context,
            CallbackInfoReturnable<ActionResult> cir) {
        levelzplus$placement.remove();
        if (!JobsAddonCompatConfig.builderPlacementBridge()
            || context.getWorld().isClient()
            || !(context.getPlayer() instanceof ServerPlayerEntity player)) return;

        JobsManager manager = ((JobsManagerAccess) player).getJobsManager();
        if (manager == null || !manager.isEmployedJob("builder")
            || manager.isJobMaxLevel("builder")) return;

        BlockPos pos = context.getBlockPos();
        levelzplus$placement.set(new Placement(player, pos.toImmutable(),
            context.getWorld().getBlockState(pos),
            manager.getJobLevel("builder"), manager.getJobXP("builder")));
    }

    @Inject(method = "place", at = @At("RETURN"), require = 1)
    private void levelzplus$afterPlace(ItemPlacementContext context,
            CallbackInfoReturnable<ActionResult> cir) {
        Placement placed = levelzplus$placement.get();
        levelzplus$placement.remove();
        if (placed == null || !cir.getReturnValue().isAccepted()) return;

        ServerPlayerEntity player = placed.player();
        BlockState after = player.getWorld().getBlockState(placed.pos());
        if (after.equals(placed.before())) return; // Unchanged world; no placement.

        boolean eligible = after.isIn(TagInit.BUILDER_PLACING_BLOCKS);
        JobsManager manager = ((JobsManagerAccess) player).getJobsManager();
        if (manager == null || !manager.isEmployedJob("builder")) return;

        // Native JobsAddon may have already processed Block.onPlaced.
        boolean paidByNative = manager.getJobLevel("builder") != placed.level()
            || manager.getJobXP("builder") != placed.xp();
        int earned = 0;
        if (eligible && !paidByNative && !manager.isJobMaxLevel("builder")) {
            int blockId = Registries.BLOCK.getRawId(after.getBlock());
            if (((PlayerAccess) player).setLastBlockId(placed.pos(), true, blockId)) {
                earned = Math.max(0, ConfigInit.CONFIG.builderXP);
                if (earned > 0) {
                    JobsServerPacket.writeS2CJobXPPacket(player, "builder", earned);
                }
            }
        }
        if (JobsAddonCompatConfig.debugBuilder()) {
            System.out.println("[LevelZ+] [Builder] placed=" +
                Registries.BLOCK.getId(after.getBlock()) +
                " eligible=" + eligible + " nativeAwarded=" + paidByNative +
                " fallbackXP=" + earned + " creative=" + player.isCreative() +
                " at=" + placed.pos());
        }
    }
}
