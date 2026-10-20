package frontline.combat.fcp.effects;

import com.atsuishio.superbwarfare.Mod;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import frontline.combat.fcp.FCP;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Brief block-light at the muzzle (MTS {@code lightObjects} / #flame analogue).
 * Placement prefers air along the barrel axis so light stays at the muzzle, not the hull center.
 *
 * <p>The light is a real {@code minecraft:light} block removed a few ticks later by queued server
 * work. That queue only lives in memory, so every light placed here is also tracked in
 * {@link #ACTIVE} and removed when the server stops (before the world is saved) or its level
 * unloads; otherwise a flash still lit at save time would be written to disk and never cleaned up.
 * Light blocks this class didn't place (e.g. player-placed ones) are never touched.
 *
 * <p>The vanilla light engine can still leave "ghost" block-light behind after these rapid
 * place/fade/remove cycles, and that light is saved with the chunk. So every chunk section a flash
 * touched is remembered in {@link #DIRTY}; once no flash has touched it for {@link #SWEEP_DELAY_TICKS},
 * the sections around it are re-lit ({@link #relightBox}) and the corrected light is re-sent to
 * nearby players. {@code /fcplights clear} runs the same re-light on demand.
 */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = FCP.MODID)
public final class MuzzleFlashLight {

    private record LightKey(ResourceKey<Level> dimension, BlockPos pos) {
    }

    /**
     * Lights currently placed by this class, mapped to their cleanup generation. The generation
     * stops an older scheduled cleanup from removing a light refreshed by a newer shot.
     */
    private static final Map<LightKey, Integer> ACTIVE = new ConcurrentHashMap<>();

    /** Per dimension: chunk sections (packed SectionPos) a flash lit, mapped to the last server tick it did. */
    private static final Map<ResourceKey<Level>, Map<Long, Integer>> DIRTY = new ConcurrentHashMap<>();

    /** Quiet time after the last flash in a section before it is re-lit (sustained fire just postpones it). */
    private static final int SWEEP_DELAY_TICKS = 40;
    private static final int SWEEP_CHECK_INTERVAL = 10;
    /** Ticks to let the (asynchronous) light engine finish before re-sending light to clients. */
    private static final int RESEND_DELAY_TICKS = 5;

    private static final int CLEAR_DEFAULT_RADIUS = 32;
    private static final int CLEAR_MAX_RADIUS = 128;

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

        Map<Long, Integer> dirty = DIRTY.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
        for (BlockPos lightPos : List.copyOf(placed)) {
            dirty.put(SectionPos.asLong(lightPos), level.getServer().getTickCount());

            LightKey key = new LightKey(level.dimension(), lightPos);
            final int cleanupGeneration = ACTIVE.merge(key, 1, Integer::sum);
            final int fadePeak = peakLevel;
            for (int tick = 0; tick < durationTicks; tick++) {
                final int tickIndex = tick;
                Mod.queueServerWork(tick, () -> {
                    if (ACTIVE.getOrDefault(key, 0) != cleanupGeneration) {
                        return;
                    }
                    int levelValue = Math.max(5, fadePeak - tickIndex * (fadePeak / Math.max(1, durationTicks)));
                    if (level.getBlockState(lightPos).is(Blocks.LIGHT)) {
                        level.setBlock(lightPos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, levelValue), 3);
                    }
                });
            }
            Mod.queueServerWork(durationTicks, () -> {
                if (ACTIVE.remove(key, cleanupGeneration)) {
                    removeIfLight(level, lightPos);
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
        boolean ours = state.is(Blocks.LIGHT) && ACTIVE.containsKey(new LightKey(level.dimension(), pos));
        if (!state.isAir() && !ours) {
            return false;
        }
        int appliedLevel = lightLevel;
        if (ours) {
            appliedLevel = Math.max(lightLevel, state.getValue(LightBlock.LEVEL));
        }
        level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, appliedLevel), 3);
        return true;
    }

    private static void removeIfLight(ServerLevel level, BlockPos pos) {
        if (level.isLoaded(pos) && level.getBlockState(pos).is(Blocks.LIGHT)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Ghost-light repair
    // ---------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || DIRTY.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        int now = server.getTickCount();
        if (now % SWEEP_CHECK_INTERVAL != 0) {
            return;
        }
        for (Map.Entry<ResourceKey<Level>, Map<Long, Integer>> entry : DIRTY.entrySet()) {
            ServerLevel level = server.getLevel(entry.getKey());
            if (level == null) {
                entry.getValue().clear();
                continue;
            }
            Set<Long> due = new HashSet<>();
            entry.getValue().entrySet().removeIf(e -> {
                if (now - e.getValue() < SWEEP_DELAY_TICKS) {
                    return false;
                }
                due.add(e.getKey());
                return true;
            });
            if (!due.isEmpty()) {
                sweepSections(level, due);
            }
        }
    }

    /** Re-lights each due section plus its 26 neighbours (a light-15 flash reaches ~14 blocks). */
    private static void sweepSections(ServerLevel level, Set<Long> sections) {
        Set<Long> area = new HashSet<>();
        for (long packed : sections) {
            int sx = SectionPos.x(packed), sy = SectionPos.y(packed), sz = SectionPos.z(packed);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        area.add(SectionPos.asLong(sx + dx, sy + dy, sz + dz));
                    }
                }
            }
        }
        Set<ChunkPos> columns = new HashSet<>();
        for (long packed : area) {
            int minX = SectionPos.sectionToBlockCoord(SectionPos.x(packed));
            int minY = SectionPos.sectionToBlockCoord(SectionPos.y(packed));
            int minZ = SectionPos.sectionToBlockCoord(SectionPos.z(packed));
            relightBox(level, minX, minY, minZ, minX + 15, minY + 15, minZ + 15, false);
            columns.add(new ChunkPos(SectionPos.x(packed), SectionPos.z(packed)));
        }
        resendLightLater(level, columns);
    }

    /**
     * Re-checks every block-lit position in the box that isn't a light source itself; real sources
     * just re-propagate, light with no source is cleared. Optionally also removes light blocks.
     *
     * @return {removed light blocks, re-checked positions}
     */
    private static int[] relightBox(ServerLevel level, int minX, int minY, int minZ,
                                    int maxX, int maxY, int maxZ, boolean removeLightBlocks) {
        LevelLightEngine lightEngine = level.getChunkSource().getLightEngine();
        minY = Math.max(minY, level.getMinBuildHeight());
        maxY = Math.min(maxY, level.getMaxBuildHeight() - 1);
        int removed = 0;
        int rechecked = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!level.hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z))) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(Blocks.LIGHT)) {
                        if (removeLightBlocks) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            removed++;
                        }
                    } else if (state.getLightEmission(level, pos) == 0
                            && level.getBrightness(LightLayer.BLOCK, pos) > 0) {
                        lightEngine.checkBlock(pos);
                        rechecked++;
                    }
                }
            }
        }
        return new int[]{removed, rechecked};
    }

    /**
     * The light engine runs on its own thread, and its corrections didn't reliably reach clients
     * until they rejoined, so once it has caught up, push the light of these chunks to everyone
     * tracking them.
     */
    private static void resendLightLater(ServerLevel level, Set<ChunkPos> columns) {
        Mod.queueServerWork(RESEND_DELAY_TICKS, () -> {
            LevelLightEngine lightEngine = level.getChunkSource().getLightEngine();
            for (ChunkPos chunkPos : columns) {
                if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
                    continue;
                }
                ClientboundLightUpdatePacket packet = new ClientboundLightUpdatePacket(chunkPos, lightEngine, null, null);
                for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(chunkPos, false)) {
                    player.connection.send(packet);
                }
            }
        });
    }

    // ---------------------------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------------------------

    // Fires before the server saves its levels on shutdown (dedicated stop or singleplayer quit),
    // so any flash still lit never reaches disk. Pending ghost-light sweeps are dropped here: the
    // light engine is asynchronous and wouldn't finish before the save; /fcplights clear covers it.
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MinecraftServer server = event.getServer();
        for (LightKey key : List.copyOf(ACTIVE.keySet())) {
            ServerLevel level = server.getLevel(key.dimension());
            if (level != null) {
                removeIfLight(level, key.pos());
            }
        }
        ACTIVE.clear();
        DIRTY.clear();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ACTIVE.keySet().removeIf(key -> {
            if (!key.dimension().equals(level.dimension())) {
                return false;
            }
            removeIfLight(level, key.pos());
            return true;
        });
        DIRTY.remove(level.dimension());
    }

    // ---------------------------------------------------------------------------------------------
    // /fcplights clear [radius]
    // ---------------------------------------------------------------------------------------------

    // Cleans up flash light left over from before these fixes: removes stray light blocks and re-lights
    // the area, which wipes "ghost" light baked into the chunks. It can't tell old muzzle-flash light
    // blocks apart from ones players placed, so it removes both.
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("fcplights")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("clear")
                                .executes(ctx -> clearLights(ctx.getSource(), CLEAR_DEFAULT_RADIUS))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1, CLEAR_MAX_RADIUS))
                                        .executes(ctx -> clearLights(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "radius"))))));
    }

    private static int clearLights(CommandSourceStack source, int radius) {
        ServerLevel level = source.getLevel();
        BlockPos center = BlockPos.containing(source.getPosition());
        int[] counts = relightBox(level,
                center.getX() - radius, center.getY() - radius, center.getZ() - radius,
                center.getX() + radius, center.getY() + radius, center.getZ() + radius, true);

        Set<ChunkPos> columns = new HashSet<>();
        for (int cx = SectionPos.blockToSectionCoord(center.getX() - radius); cx <= SectionPos.blockToSectionCoord(center.getX() + radius); cx++) {
            for (int cz = SectionPos.blockToSectionCoord(center.getZ() - radius); cz <= SectionPos.blockToSectionCoord(center.getZ() + radius); cz++) {
                columns.add(new ChunkPos(cx, cz));
            }
        }
        resendLightLater(level, columns);

        source.sendSuccess(() -> Component.literal("Removed " + counts[0] + " light block(s) and re-checked "
                + counts[1] + " lit position(s) within " + radius
                + " blocks. Note: this also removes light blocks placed by players."), true);
        return counts[0] + counts[1];
    }
}
