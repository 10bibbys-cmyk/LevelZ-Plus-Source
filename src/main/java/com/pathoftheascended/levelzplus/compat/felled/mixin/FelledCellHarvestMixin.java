package com.pathoftheascended.levelzplus.compat.felled.mixin;

import com.pathoftheascended.levelzplus.compat.felled.FelledCells;
import com.pathoftheascended.levelzplus.compat.felled.FelledHarvestHooks;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Felled's logs are Zarith physics cells, not world blocks.
 * In Zarith 0.2.0, mineCell's final return follows a successful removeCells.
 * Removed grid cells retain their original BlockState but have LIVE cleared.
 */
@Pseudo
@Mixin(targets = "com.raiiiden.zarith.group.BlockGroupEntity", remap = false)
public abstract class FelledCellHarvestMixin {
    @Inject(method = "mineCell", at = @At("HEAD"), cancellable = true, remap = false)
    private void pota$protectFelledLog(ServerPlayerEntity player, int cell, CallbackInfo ci) {
        if (!FelledCells.isFelledTree(this)) return;
        BlockState state = FelledCells.cellState(this, cell, true);
        if (state == null || !state.isIn(BlockTags.LOGS)) return;
        if (FelledHarvestHooks.denyWrongTool(this, player, cell, state)) ci.cancel();
    }

    @Inject(method = "mineCell", at = @At("TAIL"), remap = false)
    private void pota$awardHarvestedLog(ServerPlayerEntity player, int cell, CallbackInfo ci) {
        if (!FelledCells.isFelledTree(this)) return;
        BlockState removed = FelledCells.cellState(this, cell, false);
        if (removed == null || !removed.isIn(BlockTags.LOGS)) return;
        FelledHarvestHooks.onSuccessfulLogHarvest(player, removed);
    }
}
