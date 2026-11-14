package com.pathoftheascended.levelzplus.compat.jobs.mixin;

import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig;
import java.util.Optional;
import net.jobsaddon.jobs.JobHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Handheld SkilletItem#finishUsingItem is method_7861 on Fabric 1.20.1.
 * Capture the valid campfire cooking output before the item clears its NBT.
 * Credit only after finishUsingItem completes server-side.
 */
@Pseudo
@Mixin(targets = "vectorwing.farmersdelight.common.item.SkilletItem", remap = false)
public abstract class FarmersDelightSkilletJobMixin {
    @Unique private static final ThreadLocal<ItemStack> levelzplus$cooked =
        ThreadLocal.withInitial(() -> ItemStack.EMPTY);

    @Inject(method = "method_7861", at = @At("HEAD"), remap = false, require = 0)
    private void levelzplus$checkSkilletRecipe(ItemStack skillet, World world,
        LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        levelzplus$cooked.set(ItemStack.EMPTY);
        if (!JobsAddonCompatConfig.farmersDelightCookingBridge()
            || world.isClient() || !(entity instanceof ServerPlayerEntity)) return;
        NbtCompound nbt = skillet.getNbt();
        if (nbt == null || !nbt.contains("Cooking", NbtElement.COMPOUND_TYPE)) return;

        ItemStack ingredient = ItemStack.fromNbt(nbt.getCompound("Cooking"));
        if (ingredient.isEmpty()) return;
        Optional<CampfireCookingRecipe> recipe =
            world.getRecipeManager().getFirstMatch(RecipeType.CAMPFIRE_COOKING,
                new SimpleInventory(ingredient), world);
        if (recipe.isPresent()) {
            ItemStack result = recipe.get().getOutput(world.getRegistryManager());
            if (!result.isEmpty()) levelzplus$cooked.set(result.copy());
        }
    }

    @Inject(method = "method_7861", at = @At("RETURN"), remap = false, require = 0)
    private void levelzplus$onSkilletCooked(ItemStack skillet, World world,
        LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack cooked = levelzplus$cooked.get();
        levelzplus$cooked.remove();
        if (!cooked.isEmpty() && entity instanceof ServerPlayerEntity player) {
            JobHelper.addFarmerCraftingXp(player, cooked);
        }
    }
}
