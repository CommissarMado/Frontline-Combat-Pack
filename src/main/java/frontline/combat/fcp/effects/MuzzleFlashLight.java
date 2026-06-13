package frontline.combat.fcp.effects;

import com.atsuishio.superbwarfare.Mod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Brief block-light at the muzzle (MTS {@code lightObjects} / #flame analogue).
 * Placement prefers air along the barrel axis so light stays at the muzzle, not the hull center.
 */
public final class MuzzleFlashLight {

    /** Prevents an older scheduled cleanup from removing a light refreshed by a newer shot. */
    private static final Map<BlockPos, Integer> CLEANUP_GENERATION = new ConcurrentHashMap<>();

    private MuzzleFlashLight() {
    }

    public static void spawn(ServerLevel level, Vec3 muzzlePos, Vec3 forward, MuzzleEffectPreset preset) {
        int peakLevel;
        int durationTicks;
        int extraAlongBarrel;

        switch (preset) {
            case TANK -> {
                peakLevel = 15;
                durationTicks = 4;
                extraAlongBarrel = 2;
            }
            case AUTOCANNON -> {
                peakLevel = 14;
                durationTicks = 2;
                extraAlongBarrel = 1;
            }
            case MISSILE -> {
                peakLevel = 13;
                durationTicks = 2;
                extraAlongBarrel = 0;
            }
            case MACHINE_GUN -> {
                peakLevel = 11;
                durationTicks = 1;
                extraAlongBarrel = 0;
            }
            default -> {
                return;
            }
        }

        Vec3 direction = forward.lengthSqr() > 1.0E-6 ? forward.normalize() : new Vec3(0, 0, 1);
        List<BlockPos> placed = new ArrayList<>();

        placeAtMuzzle(level, muzzlePos, direction, peakLevel, placed);
        for (int i = 1; i <= extraAlongBarrel; i++) {
            Vec3 alongBarrel = muzzlePos.add(direction.scale(0.28 * i));
            placeAtMuzzle(level, alongBarrel, direction, Math.max(9, peakLevel - i * 2), placed);
        }

        if (placed.isEmpty()) {
            return;
        }

        List<BlockPos> lights = List.copyOf(placed);
        for (BlockPos lightPos : lights) {
            int generation = CLEANUP_GENERATION.merge(lightPos, 1, Integer::sum);
            final int cleanupGeneration = generation;
            final int fadePeak = peakLevel;
            for (int tick = 0; tick < durationTicks; tick++) {
                final int tickIndex = tick;
                Mod.queueServerWork(tick, () -> {
                    if (CLEANUP_GENERATION.getOrDefault(lightPos, 0) != cleanupGeneration) {
                        return;
                    }
                    int levelValue = Math.max(5, fadePeak - tickIndex * (fadePeak / Math.max(1, durationTicks)));
                    if (level.getBlockState(lightPos).is(Blocks.LIGHT)) {
                        level.setBlock(lightPos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, levelValue), 3);
                    }
                });
            }
            Mod.queueServerWork(durationTicks, () -> {
                if (CLEANUP_GENERATION.getOrDefault(lightPos, 0) != cleanupGeneration) {
                    return;
                }
                CLEANUP_GENERATION.remove(lightPos, cleanupGeneration);
                if (level.getBlockState(lightPos).is(Blocks.LIGHT)) {
                    level.setBlock(lightPos, Blocks.AIR.defaultBlockState(), 3);
                }
            });
        }
    }

    /**
     * Finds the closest placeable air block to the muzzle, searching forward along the barrel first.
     */
    private static void placeAtMuzzle(ServerLevel level, Vec3 muzzlePos, Vec3 forward, int lightLevel, List<BlockPos> placed) {
        Vec3 direction = forward.normalize();
        List<Vec3> candidates = new ArrayList<>();

        for (double step = 0.0; step <= 1.0; step += 0.1) {
            candidates.add(muzzlePos.add(direction.scale(step)));
        }
        for (double step = 0.08; step <= 0.35; step += 0.08) {
            candidates.add(muzzlePos.subtract(direction.scale(step)));
        }

        BlockPos center = BlockPos.containing(muzzlePos);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int radius = 0; radius <= 2; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0 && radius > 0) {
                            continue;
                        }
                        mutable.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                        candidates.add(Vec3.atCenterOf(mutable));
                    }
                }
            }
        }

        candidates.sort(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(muzzlePos)));

        for (Vec3 candidate : candidates) {
            BlockPos blockPos = BlockPos.containing(candidate);
            if (placed.contains(blockPos)) {
                continue;
            }
            if (tryPlace(level, blockPos, lightLevel)) {
                placed.add(blockPos);
                return;
            }
        }
    }

    private static boolean tryPlace(ServerLevel level, BlockPos pos, int lightLevel) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && !state.is(Blocks.LIGHT)) {
            return false;
        }
        int appliedLevel = lightLevel;
        if (state.is(Blocks.LIGHT)) {
            appliedLevel = Math.max(lightLevel, state.getValue(LightBlock.LEVEL));
        }
        level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, appliedLevel), 3);
        return true;
    }
}
