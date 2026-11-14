package com.pathoftheascended.levelzplus.compat.jobs;

import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/** Strict fallback for non-vanilla equipment item implementations. */
public final class JobsAddonSmitherBridge {
    private static final TagKey<Item> EXTRA_GEAR =
        TagKey.of(RegistryKeys.ITEM, new Identifier("levelz_plus", "smither_extra_gear"));
    private static final TagKey<Item> MELEE =
        TagKey.of(RegistryKeys.ITEM, new Identifier("c", "weapons/melee"));
    private static final TagKey<Item> RANGED =
        TagKey.of(RegistryKeys.ITEM, new Identifier("c", "weapons/ranged"));

    private JobsAddonSmitherBridge() {}

    public static boolean isAdditionalGear(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.isIn(EXTRA_GEAR)) return true;
        Item item = stack.getItem();
        // Standard item classes already receive native JobsAddon Smither XP.
        if (item instanceof ToolItem || item instanceof SwordItem ||
            item instanceof ArmorItem || item instanceof BowItem ||
            item instanceof CrossbowItem || item instanceof HorseArmorItem) return false;
        if (!stack.isIn(MELEE) && !stack.isIn(RANGED)) return false;
        Identifier id = Registries.ITEM.getId(item);
        if ("minecraft".equals(id.getNamespace())) return false;
        String name = id.getPath();
        return name.matches(".*(sword|dagger|axe|bow|crossbow|staff|wand|claymore|hammer|spear|rapier|knuckle|mace|scythe|trident|shield|helmet|chestplate|leggings|boots).*");
    }

    public static void award(ServerPlayerEntity player, ItemStack stack) {
        JobsManager manager = ((JobsManagerAccess) player).getJobsManager();
        if (manager == null || !manager.isEmployedJob("smither")
            || !isAdditionalGear(stack) || player.isCreative()) return;
        long amount = (long) ConfigInit.CONFIG.smitherXP * stack.getCount();
        if (amount > 0L) JobsServerPacket.writeS2CJobXPPacket(player, "smither",
            (int) Math.min(1000, amount));
    }
}
