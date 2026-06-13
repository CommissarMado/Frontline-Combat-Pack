package frontline.combat.fcp.effects;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.data.gun.ProjectileInfo;
import com.atsuishio.superbwarfare.data.gun.ShootParameters;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.particle.FCPMuzzleParticleOption;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * MTS / QMP / OfficialPack muzzle burst: bloom + bang + layered smoke with spawnEveryTick emulation.
 * Smoke uses entity-axis velocity spread (MTS generic particles — constant velocity, no drag).
 */
public final class FCPMuzzleEffects {

    /** MTS divides initialVelocity by 10 in EntityParticle. */
    private static final float MTS_VEL = 0.1f;
    private static final int HIGH_RPM_AUTOCANNON = 400;

    private FCPMuzzleEffects() {
    }

    public static boolean isFCPVehicle(VehicleEntity vehicle) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(vehicle.getType());
        return id != null && FCP.MODID.equals(id.getNamespace());
    }

    public static MuzzleEffectPreset resolvePreset(GunData gunData) {
        if (gunData == null) {
            return MuzzleEffectPreset.AUTOCANNON;
        }
        ProjectileInfo projectile = gunData.get(GunProp.PROJECTILE);
        if (projectile == null) {
            return MuzzleEffectPreset.AUTOCANNON;
        }
        String path = projectile.getId();
        if (path.contains("missile") || path.contains("rocket") || path.contains("atgm")) {
            return MuzzleEffectPreset.MISSILE;
        }
        if (path.contains("small_cannon") || path.contains("30mm")) {
            return MuzzleEffectPreset.AUTOCANNON;
        }
        if (path.contains("cannon_shell") || path.contains("mortar") || path.contains("apfsds")) {
            return MuzzleEffectPreset.TANK;
        }
        if (path.contains("projectile") || path.contains("bullet")) {
            return MuzzleEffectPreset.MACHINE_GUN;
        }
        return MuzzleEffectPreset.AUTOCANNON;
    }

    public static void spawnFromShoot(ShootParameters parameters) {
        ServerLevel level = parameters.level;
        if (level == null) {
            return;
        }
        MuzzlePositionResolver.MuzzlePose muzzle = MuzzlePositionResolver.resolve(parameters);
        Vec3 pos = muzzle.position();
        Vec3 direction = muzzle.direction();
        if (pos == null || direction == null || direction.lengthSqr() < 1.0E-6) {
            return;
        }

        MuzzleEffectPreset preset = resolvePreset(parameters.data);
        GunAxes axes = GunAxes.fromDirection(direction.normalize());
        MuzzleAnchor anchor = MuzzleAnchor.from(parameters);
        long gameTick = level.getGameTime();
        int shotIndex = anchor.isValid() ? MuzzleBurstTracker.recordShot(anchor.vehicleId(), gameTick) : 0;
        int rpm = parameters.data.get(GunProp.RPM);
        boolean sustainedAutocannon = preset == MuzzleEffectPreset.AUTOCANNON
                && rpm >= HIGH_RPM_AUTOCANNON
                && anchor.isValid()
                && MuzzleBurstTracker.isSustained(anchor.vehicleId(), gameTick);

        if (sustainedAutocannon) {
            spawnSustainedAutocannon(level, anchor, pos, axes, level.getRandom(), shotIndex);
            return;
        }

        spawnInstantFlash(level, anchor, pos, axes, level.getRandom(), preset);
        MuzzleFlashLight.spawn(level, pos, axes.forward(), preset);

        int smokeTicks = switch (preset) {
            case MACHINE_GUN -> 2;
            case AUTOCANNON -> 4;
            case TANK -> 3;
            case MISSILE -> 2;
        };

        for (int tick = 0; tick < smokeTicks; tick++) {
            final int smokeTick = tick;
            Mod.queueServerWork(smokeTick, () -> spawnSmokeTick(level, anchor, level.getRandom(), preset, smokeTick));
        }

        if (preset == MuzzleEffectPreset.TANK || preset == MuzzleEffectPreset.AUTOCANNON) {
            Mod.queueServerWork(5, () -> spawnCooldownSmoke(level, anchor, level.getRandom(), preset));
        }
    }

    private static void spawnSustainedAutocannon(
            ServerLevel level,
            MuzzleAnchor anchor,
            Vec3 pos,
            GunAxes axes,
            RandomSource random,
            int shotIndex
    ) {
        // Block light is cheap; full smoke waves were removed from sustained fire for FPS.
        MuzzleFlashLight.spawn(level, pos, axes.forward(), MuzzleEffectPreset.AUTOCANNON);

        spawnBloom(level, pos, 1f, 0.98f, 0.9f, 3.4f, 0.11f, 2);
        spawnBangSparks(level, pos, axes, random, 2, 1.4, 1.4, 1.0, 1.5f);
        spawnBlastPuff(level, anchor, pos, axes, random, 2);

        Vec3 muzzle = new Vec3(0, -0.1, 0);
        Vec3 jitter = new Vec3(0.08, 0.08, 0.12);
        Vec3 forwardVel = new Vec3(0, 0, 1.840062);
        Vec3 forwardSpread = new Vec3(1.0, 1.0, 4.0);

        spawnSmokeBatch(level, anchor, pos, axes, random, 2, 45, 1.0f, 7.0f,
                forwardVel, forwardSpread, false, muzzle, jitter, 0xC4C0BB, 6, false, 0, false);
        spawnSmokeBatch(level, anchor, pos, axes, random, 1, 35, 0.9f, 4.5f,
                forwardVel, new Vec3(0.8, 0.8, 3.0), false, muzzle, jitter, 0xC1C1C1, 5, false, 0, false);

        if (shotIndex % 3 == 0) {
            spawnSmokeBatch(level, anchor, pos, axes, random, 2, 55, 1.06f, 9.0f,
                    forwardVel, forwardSpread, false, muzzle, jitter, 0xB9B4AD, 7, false, 0, false);
        }
        if (shotIndex % 5 == 0) {
            spawnCooldownSmoke(level, anchor, random, MuzzleEffectPreset.AUTOCANNON);
        }
    }

    private static void spawnInstantFlash(ServerLevel level, MuzzleAnchor anchor, Vec3 pos, GunAxes axes, RandomSource random, MuzzleEffectPreset preset) {
        switch (preset) {
            case MACHINE_GUN -> {
                spawnBloom(level, pos, 1f, 1f, 1f, 2.8f, 0.12f, 3);
                spawnBangSparks(level, pos, axes, random, 3, 1.2, 1.2, 0.8, 1.2f);
            }
            case AUTOCANNON -> {
                spawnBloom(level, pos, 1f, 1f, 1f, 4.5f, 0.14f, 3);
                spawnBloom(level, pos, 0.98f, 0.72f, 0.45f, 3.8f, 0.12f, 3);
                spawnBangSparks(level, pos, axes, random, 6, 2.0, 2.5, 2.0, 1.8f);
                spawnBlastPuff(level, anchor, pos, axes, random, 3);
            }
            case TANK -> {
                // FT-17 style: large white + warm orange blooms (scale 64 → ~1.2 in MTS)
                spawnBloom(level, pos, 1f, 1f, 1f, 10.5f, 0.22f, 3);
                spawnBloom(level, pos, 0.99f, 0.94f, 0.71f, 8.5f, 0.18f, 3);
                spawnBloom(level, pos, 0.98f, 0.67f, 0.45f, 6.5f, 0.14f, 4);
                spawnBangStatic(level, pos, 3.6f, 4.8f, 10);
                spawnBangSparks(level, pos, axes, random, 10, 4.0, 5.0, 4.0, 2.6f);
                spawnBlastPuff(level, anchor, pos, axes, random, 6);
            }
            case MISSILE -> {
                spawnBloom(level, pos, 1f, 0.9f, 0.6f, 3.5f, 0.2f, 4);
                spawnBangSparks(level, pos, axes, random, 4, 1.5, 1.5, 1.2, 1.0f);
            }
        }
    }

    private static void spawnSmokeTick(ServerLevel level, MuzzleAnchor anchor, RandomSource random, MuzzleEffectPreset preset, int tick) {
        MuzzlePositionResolver.MuzzlePose pose = anchor.resolve(level);
        if (pose == null) {
            return;
        }
        Vec3 pos = pose.position();
        GunAxes axes = GunAxes.fromDirection(pose.direction());
        Vec3 muzzle = new Vec3(0, -0.1, 0);
        Vec3 jitter = new Vec3(0.08, 0.08, 0.12);
        Vec3 forwardSpread = new Vec3(1.0, 1.0, 5.0);
        Vec3 forwardVel = new Vec3(0, 0, 1.840062);

        switch (preset) {
            case MACHINE_GUN -> spawnSmokeBatch(level, anchor, pos, axes, random, 2, 35, 0.55f, 2.5f,
                    forwardVel, forwardSpread, false, muzzle, jitter, 0xC8C2BD, 4, false, 0, false);

            case AUTOCANNON -> {
                spawnSmokeBatch(level, anchor, pos, axes, random, 3, 80, 1.06f, 10.02f,
                        forwardVel, forwardSpread, false, muzzle, jitter, 0xC1C1C1, 9, false, 0, false);
                spawnSmokeBatch(level, anchor, pos, axes, random, 3, 40, 1.06f, 2.06f,
                        forwardVel, forwardSpread, false, muzzle, jitter, 0xC1C1C1, 4, false, 0, false);
                spawnSmokeBatch(level, anchor, pos, axes, random, 3, 40, 1.06f, 3.02f,
                        new Vec3(0, -0.30625, 0.025), forwardSpread, false, muzzle, jitter, 0xC1C1C1, 4, false, 0, false);
            }

            case TANK -> {
                if (tick == 0) {
                    spawnSmokeBatch(level, anchor, pos, axes, random, 10, 120, 4.0f, 10.0f,
                            new Vec3(0, 0, 8.0), new Vec3(2.0, 2.0, 7.0), false,
                            new Vec3(0, 0, 0.35), new Vec3(0.15, 0.15, 0.2),
                            pickTankSmokeColor(random), 9, false, 14, false);
                }
                spawnSmokeBatch(level, anchor, pos, axes, random, 4, 150, 9.0f, 20.0f,
                        new Vec3(0, 0.5, 0), new Vec3(18.0, 0.5, 18.0), true,
                        new Vec3(0, -0.15, 0), new Vec3(0.25, 0.05, 0.25),
                        0xDAD1CA, 9, false, 5, true);
                spawnSmokeBatch(level, anchor, pos, axes, random, 3, 45, 1.06f, 3.5f,
                        new Vec3(0, -0.4, 0.05), new Vec3(2.0, 2.0, 5.0), false,
                        muzzle, jitter, 0xC4C0BB, 4, false, 0, false);
            }

            case MISSILE -> spawnSmokeBatch(level, anchor, pos, axes, random, 3, 50, 1.2f, 5.0f,
                    new Vec3(0, 0, 2.5), new Vec3(1.5, 1.5, 3.0), false,
                    muzzle, jitter, 0xC8C2BD, 4, false, 0, false);
        }
    }

    private static int pickTankSmokeColor(RandomSource random) {
        int[] colors = {0xC4C0BB, 0xB9B4AD, 0xD2D0CD, 0xADA79F, 0xA39A8D, 0x99958F, 0xDDDBD8};
        return colors[random.nextInt(colors.length)];
    }

    private static void spawnCooldownSmoke(ServerLevel level, MuzzleAnchor anchor, RandomSource random, MuzzleEffectPreset preset) {
        MuzzlePositionResolver.MuzzlePose pose = anchor.resolve(level);
        if (pose == null) {
            return;
        }
        Vec3 pos = pose.position();
        GunAxes axes = GunAxes.fromDirection(pose.direction());
        int count = preset == MuzzleEffectPreset.TANK ? 4 : 3;
        spawnSmokeBatch(level, anchor, pos, axes, random, count, 45, 0.22f, 1.0f,
                new Vec3(0, 0, 0.6), new Vec3(0.5, 0.5, 0.5), false,
                new Vec3(0, 0, 0.1), new Vec3(0.05, 0.05, 0.05),
                0x7C7670, 4, true, 0, false);
    }

    private static void spawnBloom(ServerLevel level, Vec3 pos, float r, float g, float b, float from, float to, int life) {
        send(level, new FCPMuzzleParticleOption(r, g, b, life, 0.68f, 1, from, to, 1, FCPMuzzleParticleOption.LAYER_BLOOM), pos, Vec3.ZERO);
    }

    private static void spawnBangStatic(ServerLevel level, Vec3 pos, float from, float to, int life) {
        send(level, new FCPMuzzleParticleOption(1f, 1f, 1f, life, 0.75f, 1, from, to, 9, FCPMuzzleParticleOption.LAYER_BANG_STATIC), pos, Vec3.ZERO);
    }

    private static void spawnBangSparks(ServerLevel level, Vec3 pos, GunAxes axes, RandomSource random, int count, double spreadX, double spreadY, double spreadZ, float forwardSpeed) {
        for (int i = 0; i < count; i++) {
            Vec3 local = spreadVelocity(random, spreadX, spreadY, spreadZ, new Vec3(0, 0, forwardSpeed));
            Vec3 velocity = axes.toWorld(local).scale(MTS_VEL);
            send(level, new FCPMuzzleParticleOption(
                    1f, 0.95f, 0.75f, 4, 0.72f, 1, 1.0f, 0.01f, 9, FCPMuzzleParticleOption.LAYER_BANG_SPARK
            ), pos, velocity);
        }
    }

    private static void spawnBlastPuff(ServerLevel level, MuzzleAnchor anchor, Vec3 pos, GunAxes axes, RandomSource random, int count) {
        Vec3 spawn = pos.add(axes.toWorld(new Vec3(0, 0, -0.05)));
        for (int i = 0; i < count; i++) {
            Vec3 local = spreadVelocity(random, 1.0, 1.0, 5.0, new Vec3(0, 0, 19.840062));
            Vec3 velocity = axes.toWorld(local).scale(MTS_VEL);
            send(level, new FCPMuzzleParticleOption(
                    0.76f, 0.75f, 0.72f, 5, 0.995f, 2, 0.06f, 2.1f, 9, FCPMuzzleParticleOption.LAYER_SMOKE
            ), spawn, velocity, anchor);
        }
    }

    private static void spawnSmokeBatch(
            ServerLevel level,
            MuzzleAnchor anchor,
            Vec3 pos,
            GunAxes axes,
            RandomSource random,
            int count,
            int life,
            float mtsFromScale,
            float mtsToScale,
            Vec3 localVelocity,
            Vec3 spread,
            boolean worldVelocity,
            Vec3 muzzleOffset,
            Vec3 spawnJitter,
            int rgb,
            int animSpeed,
            boolean linger,
            int movementDuration,
            boolean worldSpawnJitter
    ) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;

        Vec3 baseSpawn = pos.add(worldSpawnJitter ? muzzleOffset : axes.toWorld(muzzleOffset));

        for (int i = 0; i < count; i++) {
            Vec3 jitter = new Vec3(
                    (random.nextDouble() * 2.0 - 1.0) * spawnJitter.x,
                    (random.nextDouble() * 2.0 - 1.0) * spawnJitter.y,
                    (random.nextDouble() * 2.0 - 1.0) * spawnJitter.z
            );
            Vec3 spawnPos = worldSpawnJitter ? baseSpawn.add(jitter) : baseSpawn.add(axes.toWorld(jitter));

            Vec3 local = spreadVelocity(random, spread.x, spread.y, spread.z, localVelocity);
            Vec3 velocity = (worldVelocity ? local : axes.toWorld(local)).scale(MTS_VEL);

            send(level, new FCPMuzzleParticleOption(
                    r, g, b,
                    life + random.nextInt(Math.max(1, life / 8)),
                    linger ? 0.998f : 0.9992f,
                    animSpeed,
                    mtsFromScale,
                    mtsToScale,
                    9,
                    FCPMuzzleParticleOption.LAYER_SMOKE,
                    linger,
                    movementDuration
            ), spawnPos, velocity, anchor);
        }
    }

    private static Vec3 spreadVelocity(RandomSource random, double spreadX, double spreadY, double spreadZ, Vec3 base) {
        return new Vec3(
                base.x + (random.nextDouble() * 2.0 - 1.0) * spreadX,
                base.y + (random.nextDouble() * 2.0 - 1.0) * spreadY,
                base.z + (random.nextDouble() * 2.0 - 1.0) * spreadZ
        );
    }

    private static void send(ServerLevel level, FCPMuzzleParticleOption option, Vec3 pos, Vec3 velocity) {
        send(level, option, pos, velocity, MuzzleAnchor.NONE);
    }

    private static void send(ServerLevel level, FCPMuzzleParticleOption option, Vec3 pos, Vec3 velocity, MuzzleAnchor anchor) {
        FCPMuzzleParticleOption payload = anchor.isValid()
                && option.layer() == FCPMuzzleParticleOption.LAYER_SMOKE
                && option.lingerSmoke()
                ? option.withBarrelAttach(anchor.vehicleId(), anchor.seatIndex())
                : option;
        level.sendParticles(payload, pos.x, pos.y, pos.z, 0, velocity.x, velocity.y, velocity.z, 1.0);
    }
}