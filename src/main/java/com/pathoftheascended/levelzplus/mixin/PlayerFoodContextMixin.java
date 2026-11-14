package com.pathoftheascended.levelzplus.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.pathoftheascended.levelzplus.FoodBridge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public abstract class PlayerFoodContextMixin {
   @WrapMethod(method = "eatFood")
   private ItemStack levelzplus$withFoodContext(World world, ItemStack stack, Operation<ItemStack> original) {
      FoodBridge.begin((PlayerEntity)(Object)this);
      try {
         return original.call(world, stack);
      } finally {
         FoodBridge.end();
      }
   }
}
