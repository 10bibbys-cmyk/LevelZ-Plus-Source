package com.pathoftheascended.levelzplus.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.pathoftheascended.levelzplus.FortuneContext;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public abstract class BlockLootMixin {
   @WrapMethod(
      method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;"
   )
   private static List<ItemStack> levelzplus$withLuckFortune(
      BlockState state,
      ServerWorld world,
      BlockPos pos,
      BlockEntity blockEntity,
      Entity entity,
      ItemStack stack,
      Operation<List<ItemStack>> original
   ) {
      FortuneContext.begin(entity);
      try {
         return original.call(state, world, pos, blockEntity, entity, stack);
      } finally {
         FortuneContext.end();
      }
   }
}
