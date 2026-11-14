package com.pathoftheascended.levelzplus.mixin;

import com.pathoftheascended.levelzplus.FortuneContext;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.class_1890", remap = false)
public abstract class EnchantmentHelperMixin {
   @Inject(method = "method_8225(Lnet/minecraft/class_1887;Lnet/minecraft/class_1799;)I", at = @At("RETURN"), cancellable = true, require = 1, remap = false)
   private static void levelzplus$addVirtualFortune(Enchantment var0, ItemStack var1, CallbackInfoReturnable<Integer> var2) {
      if (var0 == Enchantments.FORTUNE) {
         int var3 = FortuneContext.currentBonus();
         if (var3 > 0) {
            Integer var4 = (Integer)var2.getReturnValue();
            var2.setReturnValue((var4 == null ? 0 : var4) + var3);
         }
      }
   }
}
