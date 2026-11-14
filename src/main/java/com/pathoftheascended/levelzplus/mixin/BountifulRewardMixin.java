package com.pathoftheascended.levelzplus.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.pathoftheascended.levelzplus.TradeRewardBridge;
import io.ejekta.bountiful.bounty.BountyDataEntry;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.ejekta.bountiful.bounty.types.builtin.BountyTypeItem", remap = false)
public abstract class BountifulRewardMixin {
   private static final ThreadLocal<PlayerEntity> LEVELZPLUS_PLAYER = new ThreadLocal<>();

   @WrapMethod(method = "giveReward")
   private boolean levelzplus$withPlayerContext(
      BountyDataEntry entry,
      PlayerEntity player,
      Operation<Boolean> original
   ) {
      LEVELZPLUS_PLAYER.set(player);
      try {
         return original.call(entry, player);
      } finally {
         LEVELZPLUS_PLAYER.remove();
      }
   }

   @Redirect(method = "giveReward", at = @At(value = "INVOKE", target = "Lio/ejekta/bountiful/bounty/BountyDataEntry;getAmount()I"), require = 0)
   private int levelzplus$scaleCurrencyAmount(BountyDataEntry entry) {
      return TradeRewardBridge.scaleCurrencyAmount(LEVELZPLUS_PLAYER.get(), entry.getContent(), entry.getAmount());
   }
}
