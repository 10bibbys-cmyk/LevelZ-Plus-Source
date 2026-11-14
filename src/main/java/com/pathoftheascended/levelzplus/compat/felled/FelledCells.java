package com.pathoftheascended.levelzplus.compat.felled;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.block.BlockState;
import org.joml.Vector3d;

/**
 * Narrow, version-matched adapter for Zarith 0.2.0's physics grid.
 * Kept reflective to avoid requiring Felled's newer Loom 1.18.2 build metadata
 * in LevelZ+'s Fabric Loom 1.8 build.
 */
public final class FelledCells {
    private static final String FELLED_CLASS = "com.raiiiden.felled.entity.FallingTreeEntity";
    private static boolean initialized;
    private static boolean available;
    private static boolean warned;
    private static Field gridField;
    private static Method exists;
    private static Method isLive;
    private static Method state;
    private static Method cellToWorld;

    private FelledCells() {}

    private static synchronized void init() {
        if (initialized) return;
        initialized = true;
        try {
            Class<?> group = Class.forName("com.raiiiden.zarith.group.BlockGroupEntity");
            Class<?> grid = Class.forName("com.raiiiden.zarith.group.BlockGrid");
            gridField = group.getDeclaredField("grid");
            gridField.setAccessible(true);
            exists = grid.getMethod("exists", int.class);
            isLive = grid.getMethod("isLive", int.class);
            state = grid.getMethod("state", int.class);
            cellToWorld = group.getMethod("cellToWorld", int.class, Vector3d.class);
            available = true;
        } catch (ReflectiveOperationException error) {
            warn(error);
        }
    }

    public static boolean isFelledTree(Object entity) {
        for (Class<?> type = entity.getClass(); type != null; type = type.getSuperclass()) {
            if (FELLED_CLASS.equals(type.getName())) return true;
        }
        return false;
    }

    public static BlockState cellState(Object entity, int index, boolean requireLive) {
        init();
        if (!available || index < 0) return null;
        try {
            Object grid = gridField.get(entity);
            if (grid == null || !Boolean.TRUE.equals(exists.invoke(grid, index))) return null;
            boolean live = Boolean.TRUE.equals(isLive.invoke(grid, index));
            if (live != requireLive) return null;
            return (BlockState) state.invoke(grid, index);
        } catch (ReflectiveOperationException error) {
            warn(error);
            return null;
        }
    }

    public static Vector3d cellPosition(Object entity, int index) {
        init();
        if (!available) return null;
        try {
            return (Vector3d) cellToWorld.invoke(entity, index, new Vector3d());
        } catch (ReflectiveOperationException error) {
            warn(error);
            return null;
        }
    }

    private static synchronized void warn(Exception error) {
        if (warned) return;
        warned = true;
        System.err.println("[LevelZ+] [Felled] Zarith 0.2.0 cell API mismatch: " + error);
    }
}
