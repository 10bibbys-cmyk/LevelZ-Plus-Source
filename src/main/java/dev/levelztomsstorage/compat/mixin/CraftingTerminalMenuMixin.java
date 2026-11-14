package dev.levelztomsstorage.compat.mixin;

import dev.levelztomsstorage.compat.LevelZCraftingGuard;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.tom.storagemod.gui.CraftingTerminalMenu", remap = false)
public abstract class CraftingTerminalMenuMixin {
   @Shadow(remap = false)
   public abstract Slot getCraftingResultSlot();

   @Inject(method = "shiftClickItems", at = @At("HEAD"), cancellable = true, remap = false)
   private void levelztomsstorage$enforceLevelZOnShiftCraft(PlayerEntity var1, int var2, CallbackInfoReturnable<ItemStack> var3) {
      if (var2 == 0) {
         Slot var4 = this.getCraftingResultSlot();
         if (var4 != null && LevelZCraftingGuard.isBlocked(var1, var4.getStack())) {
            var3.setReturnValue(ItemStack.EMPTY);
         }
      }
   }
}
