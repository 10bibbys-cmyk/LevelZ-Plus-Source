package com.pathoftheascended.levelzplus.compat.jobs.mixin;

import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig;
import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonFisherBridge;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tide 2.1.1 Fabric 1.20.1: retrieve rod, world, player.
 * Called immediately before the bobber clears catchType at startRetrieving().
 * This point follows actual item spawning, so unsuccessful reels earn no XP.
 */
@Pseudo
@Mixin(targets = "com.li64.tide.registries.entities.misc.fishing.TideFishingHook", remap = false)
public abstract class TideHookRetrieveMixin {
    @Inject(
        method = "retrieve(Lnet/minecraft/class_1799;Lnet/minecraft/class_3218;Lnet/minecraft/class_1657;)I",
        at = @At(value = "INVOKE",
                 target = "Lcom/li64/tide/registries/entities/misc/fishing/TideFishingHook;startRetrieving()V"),
        require = 0, remap = false
    )
    private void levelzplus$rewardFishOnRetrieve(ItemStack rod, ServerWorld world,
        PlayerEntity player, CallbackInfoReturnable<Integer> cir) {
        if (!JobsAddonCompatConfig.tideFisherBridge()
            || !(player instanceof ServerPlayerEntity server)) return;
        try {
            Method catchTypeMethod = this.getClass().getMethod("getCatchType");
            Object catchType = catchTypeMethod.invoke(this);
            if (!(catchType instanceof Enum<?> type) || !type.name().equals("FISH")) return;

            Method hookedItemsMethod = this.getClass().getMethod("getHookedItems");
            Object caught = hookedItemsMethod.invoke(this);
            if (!(caught instanceof List<?> fish)) return;

            for (Object item : fish) {
                if (item instanceof ItemStack stack && !stack.isEmpty()) {
                    JobsAddonFisherBridge.awardFish(server, stack);
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Never make failed optional reflection interfere with fishing.
        }
    }
}
