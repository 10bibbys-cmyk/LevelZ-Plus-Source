package com.pathoftheascended.levelzplus.mixin;

import com.pathoftheascended.levelzplus.LevelZExperienceBridge;
import com.pathoftheascended.levelzplus.ProgressionXpConfig;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "actuallyharvest.event.HarvestEventHandler", remap = false)
public abstract class ActuallyHarvestXpMixin {
   @Inject(method = "harvestAndReplant", at = @At("RETURN"), require = 1, remap = false)
   private static void levelzplus$awardHarvestXp(
      World level,
      BlockPos pos,
      BlockState blockState,
      LivingEntity entity,
      Hand hand,
      CallbackInfoReturnable<Boolean> info
   ) {
      if (!ProgressionXpConfig.actuallyHarvestEnabled()
         || !ProgressionXpConfig.independentXpReady()
         || !Boolean.TRUE.equals(info.getReturnValue())
         || !(entity instanceof ServerPlayerEntity player)
         || player.networkHandler == null) {
         return;
      }

      int reward = LevelZExperienceBridge.scale(
         ProgressionXpConfig.rollActuallyHarvestBaseXp(),
         ProgressionXpConfig.actuallyHarvestXpMultiplier()
      );
      if (reward > 0) {
         LevelZExperienceBridge.award(player, reward, "actually_harvest:" + blockState.getBlock());
      }
   }
}
