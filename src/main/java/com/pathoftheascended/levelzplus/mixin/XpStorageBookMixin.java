package com.pathoftheascended.levelzplus.mixin;

import com.pathoftheascended.levelzplus.XpStorageBridge;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.github.charlyb01.xpstorage.XpBook", remap = false)
public abstract class XpStorageBookMixin {
   @Inject(method = "method_7836", at = @At("HEAD"), remap = false, require = 1)
   private void levelzplus$armWithdrawalFailsafe(World var1, PlayerEntity var2, Hand var3, CallbackInfoReturnable<TypedActionResult<ItemStack>> var4) {
      if (var2 != null && var2.isSneaking()) {
         XpStorageBridge.armBookWithdrawal(var2);
      }
   }
}
