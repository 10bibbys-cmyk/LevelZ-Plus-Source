package com.pathoftheascended.levelzplus.compat.felled;

import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig;
import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonFelledBridge;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

/** Server-authoritative companion to PotA's KubeJS standing-log protection. */
public final class FelledHarvestHooks {
    private static final TagKey<net.minecraft.item.Item> RUNE_AXES =
        TagKey.of(RegistryKeys.ITEM, new Identifier("death_knights", "great_runeaxes"));
    private static final TagKey<net.minecraft.block.Block> RU_BRANCHES =
        TagKey.of(RegistryKeys.BLOCK, new Identifier("regions_unexplored", "branches"));
    private static final Map<UUID, Long> LAST_HURT = new HashMap<>();
    private static final Map<UUID, Long> LAST_MESSAGE = new HashMap<>();

    private FelledHarvestHooks() {}

    public static boolean denyWrongTool(Object tree, ServerPlayerEntity player,
                                         int cell, BlockState state) {
        if (!JobsAddonCompatConfig.protectFelledLogs() || player.isCreative()
            || state.isIn(RU_BRANCHES) || !state.isIn(BlockTags.LOGS)) {
            return false;
        }
        ItemStack held = player.getMainHandStack();
        boolean axe = !held.isEmpty() &&
            (held.isIn(ItemTags.AXES) || held.isIn(RUNE_AXES)
                || held.isOf(net.minecraft.registry.Registries.ITEM.get(
                    new Identifier("notreepunching", "flint_axe"))));
        if (axe) return false;

        long tick = player.getWorld().getTime();
        UUID id = player.getUuid();
        if (held.isEmpty() && ready(LAST_HURT, id, tick, 10)) {
            player.damage(player.getDamageSources().generic(), 1.0F);
        }
        if (ready(LAST_MESSAGE, id, tick, 10)) {
            if (held.isEmpty()) {
                player.sendMessage(Text.literal(
                    "The bark splits my skin before the wood gives way. I need an axe."
                ).formatted(Formatting.RED), true);
            } else {
                Vector3d spot = FelledCells.cellPosition(tree, cell);
                if (spot == null) spot = new Vector3d(player.getX(), player.getY() + 1, player.getZ());
                ((ServerWorld) player.getWorld()).spawnParticles(
                    ParticleTypes.CRIT, spot.x, spot.y, spot.z, 6,
                    0.08, 0.08, 0.08, 0.10
                );
                player.sendMessage(Text.literal(
                    "This tool cannot bite into the trunk. I need an axe."
                ).formatted(Formatting.YELLOW), true);
            }
        }
        return true;
    }

    private static boolean ready(Map<UUID, Long> map, UUID id, long now, long cooldown) {
        // World time may reset when moving dimensions; never suppress forever.
        Long previous = map.get(id);
        if (previous != null && now >= previous && now - previous < cooldown) return false;
        map.put(id, now);
        return true;
    }

    public static void onSuccessfulLogHarvest(ServerPlayerEntity player, BlockState state) {
        if (player.isCreative() || !state.isIn(BlockTags.LOGS)) return;
        if (FabricLoader.getInstance().isModLoaded("jobsaddon")
            && JobsAddonCompatConfig.enabled()
            && JobsAddonCompatConfig.felledLumberjackBridge()) {
            // Isolated class: don't resolve JobsAddon classes when that mod is absent.
            JobsAddonFelledBridge.onHarvest(player, state);
        }
    }
}
