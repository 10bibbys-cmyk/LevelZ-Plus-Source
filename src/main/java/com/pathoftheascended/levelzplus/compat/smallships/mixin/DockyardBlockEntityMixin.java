package com.pathoftheascended.levelzplus.compat.smallships.mixin;

import com.pathoftheascended.levelzplus.compat.smallships.SmallShipsCraftingGuard;
import com.talhanation.smallships.api.ShipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.talhanation.smallships.world.block.DockyardBlockEntity", remap = false)
public abstract class DockyardBlockEntityMixin {
   @Inject(method = "startBuildShip", at = @At("HEAD"), cancellable = true, remap = false)
   private void levelzplus$enforceShipCraftingRequirement(ServerPlayerEntity var1, ShipType var2, Type var3, CallbackInfo var4) {
      if (SmallShipsCraftingGuard.shouldBlock(var1, var2, var3)) {
         var4.cancel();
      }
   }
}
