package frontline.combat.fcp.client.particle;

import com.atsuishio.superbwarfare.client.particle.CustomCloudOption;
import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo;
import com.atsuishio.superbwarfare.data.vehicle.subdata.VehicleType;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.tools.SpritePixelHelper;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.init.ModParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * Rotor downwash on FCP's helicopters, client side only. Every surface gets a veil of large, transparent dust; on top of it grass
 * throws up clippings, leaves leaf flecks, sand and snow SBW's vehicle ground dust and a thicker veil in their colour, gravel and
 * soil block chips, and water foam under a bluer veil.
 * <p>
 * A fixed number of spots under the rotor are tried each tick and the surface there decides what they throw up, which keeps one
 * helicopter to about 150 live particles at most.
 */
@EventBusSubscriber(modid = FCP.MODID, value = Dist.CLIENT)
public final class HelicopterDownwash {

    /** A main rotor: pivot along the hull and blade tip distance in blocks, measured from the geo's rotor bone and blade cubes. */
    private record Rotor(double z, double radius) {
    }

    private static final Map<String, Rotor[]> ROTORS = new HashMap<>();

    static {
        rotors(new Rotor[]{new Rotor(0.99, 25.05)}, "bigbird");
        rotors(new Rotor[]{new Rotor(6.22, 10.03), new Rotor(-6.56, 10.03)}, "ch47");
        rotors(new Rotor[]{new Rotor(-0.05, 13.17)}, "ch53a", "ch53e", "mh53");
        rotors(new Rotor[]{new Rotor(0.45, 5.76)}, "huey");
        rotors(new Rotor[]{new Rotor(-1.35, 7.68)}, "huey_m134_door_guns", "huey_m134_gunship", "huey_m60_door_guns", "huey_m60_gunship");
        rotors(new Rotor[]{new Rotor(0.26, 7.27)}, "ka52");
        rotors(new Rotor[]{new Rotor(1.0, 4.69)}, "littlebird");
        rotors(new Rotor[]{new Rotor(1.03, 4.69)}, "littlebird_armed", "littlebird_heavy");
        rotors(new Rotor[]{new Rotor(0.2, 9.42)}, "mh60l", "uh60", "uh60_minigun");
        rotors(new Rotor[]{new Rotor(0.8, 10.37)}, "mi8", "mi8_amtsh", "mi8_door_guns", "mi8_mtv", "mi17", "mi17_door_guns");
        rotors(new Rotor[]{new Rotor(0, 12.57)}, "oh1");
        rotors(new Rotor[]{new Rotor(-1.6, 6.83)}, "venom", "venom_door_guns", "venom_gunship");
        rotors(new Rotor[]{new Rotor(0.81, 8.39)}, "viper");
    }

    private static void rotors(Rotor[] rotors, String... ids) {
        for (String id : ids) ROTORS.put(id, rotors);
    }

    /** Spots tried per tick and helicopter; every particle rate below is a chance per spot. */
    private static final int SPOTS = 6;
    private static final double MAX_DISTANCE_SQR = 128 * 128;
    /** The effect fades out at this many rotor radii above the ground. */
    private static final double REACH_RADII = 1.6;
    /** Below this share of hover power the rotor isn't moving enough air. */
    private static final float MIN_STRENGTH = 0.3f;

    private static final float VEIL = 0.25f, CLIPPINGS = 0.125f, LEAVES = 0.2f, CHIPS = 0.25f, GROUND_DUST = 0.14f, FOAM = 0.3f;
    /** Sand and snow lift far more: a thicker veil, more often, a little darker than the block's map colour. */
    private static final float VEIL_DENSE = 0.35f, VEIL_ALPHA = 0.14f, VEIL_ALPHA_DENSE = 0.3f, DENSE_SHADE = 0.88f;
    private static final int VEIL_COLOR = 0xDDD9CF, WATER_VEIL_COLOR = 0x8CB6EC;
    /** Veil speed in rotor radii per tick: with its drag it shoots out about 1.5 to 2.5 radii, well past the blade tips. */
    private static final double VEIL_SPEED = 0.12, VEIL_SPEED_SPREAD = 0.08;
    /** Radians per tick the two gusts sweep round the rotor, and how wide each is either side: the dust moves in patches, not an even ring. */
    private static final double GUST_SWEEP = 0.35, GUST_WIDTH = 0.7;

    private HelicopterDownwash() {
    }

    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null || mc.isPaused()) return;
        ParticleStatus status = mc.options.particles().get();
        if (status == ParticleStatus.MINIMAL) return;
        float budget = status == ParticleStatus.DECREASED ? 0.5f : 1f;
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof VehicleEntity vehicle) || vehicle.getVehicleType() != VehicleType.HELICOPTER) continue;
            if (vehicle.distanceToSqr(mc.player) > MAX_DISTANCE_SQR) continue;
            ResourceLocation id = EntityType.getKey(vehicle.getType());
            if (!id.getNamespace().equals(FCP.MODID)) continue;
            Rotor[] rotors = ROTORS.get(id.getPath());
            if (rotors != null) blow(mc, level, vehicle, rotors, budget);
        }
    }

    /** Share of hover power (lift x 0.66 x rotor speed = gravity, VehicleEngineUtils.helicopterEngine), up to 1. */
    private static float strength(VehicleEntity vehicle) {
        if (!(vehicle.getEngineInfo() instanceof EngineInfo.Helicopter engine) || engine.getLiftSpeed() <= 0f) return 0f;
        return Mth.clamp(vehicle.getSynchedPropellerRot() * engine.getLiftSpeed() * 0.66f / (float) vehicle.computed().getGravity(), 0f, 1f);
    }

    private static void blow(Minecraft mc, ClientLevel level, VehicleEntity vehicle, Rotor[] rotors, float budget) {
        float strength = strength(vehicle);
        if (strength < MIN_STRENGTH) return;
        RandomSource random = vehicle.getRandom();
        double yaw = Math.toRadians(vehicle.getYRot()), forwardX = -Math.sin(yaw), forwardZ = Math.cos(yaw);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int spot = 0; spot < SPOTS; spot++) {
            Rotor rotor = rotors[random.nextInt(rotors.length)];
            double gust = vehicle.tickCount * GUST_SWEEP + (spot % 2) * Math.PI;
            double angle = gust + (random.nextDouble() * 2 - 1) * GUST_WIDTH, along = rotor.radius() * (0.15 + 0.5 * random.nextDouble());
            double outX = Math.cos(angle), outZ = Math.sin(angle);
            double x = vehicle.getX() + forwardX * rotor.z() + outX * along, z = vehicle.getZ() + forwardZ * rotor.z() + outZ * along;
            int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, Mth.floor(x), Mth.floor(z));
            double height = vehicle.getY() - top;
            // Nothing from a roof or hillside above the hull.
            if (height < -1) continue;
            double closeness = 1 - height / (rotor.radius() * REACH_RADII);
            if (closeness <= 0) continue;
            float chance = strength * (float) closeness * budget;
            double push = (0.25 + 0.25 * random.nextDouble()) * closeness;
            pos.set(x, top - 1, z);
            BlockState state = level.getBlockState(pos);
            float veilSize = (float) Mth.clamp(rotor.radius() * 0.975, 4.5, 27);
            double veilPush = rotor.radius() * (VEIL_SPEED + VEIL_SPEED_SPREAD * random.nextDouble()) * (0.5 + 0.5 * closeness);
            spawn(mc, level, state, pos, random, x, top, z, outX * push, outZ * push, outX * veilPush, outZ * veilPush, chance, veilSize);
        }
    }

    private static void spawn(Minecraft mc, ClientLevel level, BlockState state, BlockPos pos, RandomSource random,
                              double x, double y, double z, double vx, double vz, double veilVx, double veilVz, float chance, float veilSize) {
        if (state.is(BlockTags.SAND) || state.is(BlockTags.SNOW)) {
            veil(mc, state.getMapColor(level, pos).col, DENSE_SHADE, VEIL_DENSE, VEIL_ALPHA_DENSE, veilSize, random, x, y, z, veilVx, veilVz, chance);
            if (random.nextFloat() < chance * GROUND_DUST) groundDust(mc, level, state, random, x, y, z, vx, vz);
            return;
        }
        veil(mc, state.getFluidState().is(FluidTags.WATER) ? WATER_VEIL_COLOR : VEIL_COLOR, 1f, VEIL, VEIL_ALPHA, veilSize, random, x, y, z, veilVx, veilVz, chance);
        if (state.getFluidState().is(FluidTags.WATER)) {
            if (random.nextFloat() < chance * FOAM) {
                level.addAlwaysVisibleParticle(new CustomCloudOption(0.92f, 0.95f, 1f, 18, 1.2f, 0.02f, false, false), true, x, y + 0.1, z, vx, 0.08, vz);
                level.addAlwaysVisibleParticle(ParticleTypes.SPLASH, true, x, y + 0.1, z, vx, 0.2, vz);
            }
        } else if (state.is(BlockTags.LEAVES)) {
            if (random.nextFloat() < chance * LEAVES) leaf(mc, level, state, pos, random, x, y, z, vx, vz);
        } else if (state.is(Blocks.GRASS_BLOCK) || (!state.liquid() && state.getCollisionShape(level, pos).isEmpty())) {
            if (random.nextFloat() < chance * CLIPPINGS) clipping(mc, level, pos, random, x, y, z, vx, vz);
        } else if (state.is(Blocks.GRAVEL) || state.is(BlockTags.DIRT)) {
            if (random.nextFloat() < chance * CHIPS) chip(mc, state, random, x, y, z, vx, vz);
        }
    }

    private static void veil(Minecraft mc, int color, float shade, float rate, float alpha, float size, RandomSource random,
                             double x, double y, double z, double vx, double vz, float chance) {
        // Shader packs can't draw the soft render type, and vanilla's looks too poor for this.
        if (random.nextFloat() >= chance * rate || FCPParticleRenderTypes.shaderPackInUse()) return;
        // Starts smaller and spreads to about the full size as it slows.
        float scale = size * (0.55f + random.nextFloat() * 0.25f);
        Particle particle = mc.particleEngine.createParticle(ModParticleTypes.DOWNWASH_DUST.get(), x, y + scale * 0.2, z, vx, 0.005, vz);
        if (!(particle instanceof DownwashDustParticle dust)) return;
        tint(dust, color, shade);
        dust.setPeakAlpha(alpha);
        dust.scale(scale);
    }

    /** Coloured like the leaf block: biome and species tinted leaves by their block colour, the untinted ones (cherry, azalea) by map colour. */
    private static void leaf(Minecraft mc, ClientLevel level, BlockState state, BlockPos pos, RandomSource random, double x, double y, double z, double vx, double vz) {
        Particle particle = mc.particleEngine.createParticle(ModParticleTypes.LEAF_FLECK.get(), x, y + 0.1, z, vx * 1.2, 0.2 + 0.25 * random.nextDouble(), vz * 1.2);
        if (particle == null) return;
        int color = mc.getBlockColors().getColor(state, level, pos, 0);
        tint(particle, color == -1 ? state.getMapColor(level, pos).col : color, 1f);
    }

    private static void clipping(Minecraft mc, ClientLevel level, BlockPos pos, RandomSource random, double x, double y, double z, double vx, double vz) {
        Particle particle = mc.particleEngine.createParticle(ModParticleTypes.GRASS_CLIPPING.get(), x, y + 0.1, z, vx * 1.2, 0.2 + 0.25 * random.nextDouble(), vz * 1.2);
        if (particle != null) tint(particle, BiomeColors.getAverageGrassColor(level, pos), 0.85f);
    }

    /** SBW's own vehicle dust on sand and snow (VehicleMotionUtils.terrainCompact), with its colour, size and rise. */
    private static void groundDust(Minecraft mc, ClientLevel level, BlockState state, RandomSource random, double x, double y, double z, double vx, double vz) {
        int color = SpritePixelHelper.INSTANCE.getRandomPixelRGB(mc.getModelManager().getBlockModelShaper().getBlockModel(state).getParticleIcon(), 0);
        float size = 1f + 7f * (float) Math.min(Math.hypot(vx, vz), 0.5) + random.nextFloat() * 2f;
        level.addAlwaysVisibleParticle(new CustomCloudOption(color, 70, size, random.nextFloat() * -0.12f, false, false), true, x, y + 0.2, z, vx, 0.02, vz);
    }

    private static void chip(Minecraft mc, BlockState state, RandomSource random, double x, double y, double z, double vx, double vz) {
        Particle particle = mc.particleEngine.createParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), x, y + 0.1, z, 0, 0, 0);
        if (particle == null) return;
        particle.setParticleSpeed(vx * 1.4, 0.2 + 0.2 * random.nextDouble(), vz * 1.4);
        particle.setLifetime(18 + random.nextInt(10));
        particle.scale(0.7f);
    }

    private static void tint(Particle particle, int color, float shade) {
        particle.setColor((color >> 16 & 255) / 255f * shade, (color >> 8 & 255) / 255f * shade, (color & 255) / 255f * shade);
    }
}
