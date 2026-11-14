package dev.levelztomsstorage.compat.mixin;

import dev.levelztomsstorage.compat.LevelZCraftingGuard;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_1703", remap = false)
public abstract class ScreenHandlerMixin {
   private static final String TOMS_CRAFTING_MENU = "com.tom.storagemod.gui.CraftingTerminalMenu";

   @Inject(method = "method_7593", at = @At("HEAD"), cancellable = true, remap = false)
   private void levelztomsstorage$enforceLevelZOnTomsCraftingTerminal(
      int slotIndex,
      int button,
      SlotActionType actionType,
      PlayerEntity player,
      CallbackInfo ci
   ) {
      if (slotIndex != 0 || !levelztomsstorage$isTomsCraftingMenu(this)) {
         return;
      }

      ScreenHandler handler = (ScreenHandler)(Object)this;
      Slot resultSlot = handler.getSlot(0);
      if (resultSlot != null && LevelZCraftingGuard.isBlocked(player, resultSlot.getStack())) {
         ci.cancel();
      }
   }

   private static boolean levelztomsstorage$isTomsCraftingMenu(Object instance) {
      for (Class<?> type = instance.getClass(); type != null; type = type.getSuperclass()) {
         if (TOMS_CRAFTING_MENU.equals(type.getName())) {
            return true;
         }
      }
      return false;
   }
}
