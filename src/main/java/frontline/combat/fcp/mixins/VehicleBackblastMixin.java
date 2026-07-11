package frontline.combat.fcp.mixins;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.tools.ParticleTool;
import frontline.combat.fcp.client.particle.FCPMuzzleParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

/**
 * Rear-only backblast for data-driven FCP launchers.
 *
 * SBW's Type 63 hardcodes its backblast inside Type63Entity.shoot(), so JSON-defined
 * vehicles get nothing. afterShoot() is the hook: it is `open`, server-side, and runs
 * from every vehicleShoot() overload immediately after the projectile spawns.
 *
 * Visual: an RPG-7-style puff (ParticleTool.sendParticle, same call RpgItem makes),
 * but roughly 2x the size, using CAMPFIRE_COSY_SMOKE - the exact particle
 * MediumRocketEntity lays down as its exhaust trail via largeTrail(). It vents from
 * the BREECH of the tube that just fired. Nothing is emitted forwards.
 */
@Mixin(value = VehicleEntity.class, remap = false)
public abstract class VehicleBackblastMixin {

    private static final Set<String> FCP_BACKBLAST_VEHICLES = Set.of(
            "fcp:ural_grad"
    );

    /** Tube length in blocks (59.28237 px / 16) - steps from the muzzle back to the breech. */
    private static final double FCP_TUBE_LENGTH = 3.7052;

    @Inject(method = "afterShoot", at = @At("TAIL"), remap = false)
    private void fcp$backblast(GunData gunData, Vec3 shootVec, CallbackInfo ci) {
        VehicleEntity self = (VehicleEntity) (Object) this;

        String vehicleId = EntityType.getKey(self.getType()).toString();
        if (!FCP_BACKBLAST_VEHICLES.contains(vehicleId)) return;
        if (!(self.level() instanceof ServerLevel level)) return;

        Vec3 barrelVector = self.getBarrelVector(1f);
        Vec3 back = barrelVector.scale(-1.0);

        int turretIndex = self.computed().getTurretControllerIndex();
        Entity gunner = self.getNthEntity(turretIndex);

        // ---- damage + knockback, behind the launcher only ----
        AABB box = new AABB(self.getBoundingBox().getCenter(), self.getBoundingBox().getCenter())
                .inflate(0.75)
                .move(barrelVector.scale(-2.0))
                .expandTowards(barrelVector.scale(-5.0));

        List<Entity> caught = level.getEntities(
                EntityTypeTest.forClass(Entity.class), box,
                e -> e != self && e.getVehicle() != self);

        for (Entity entity : caught) {
            float dist = entity.distanceTo(self);
            entity.hurt(ModDamageTypes.causeBurnDamage(level.registryAccess(), gunner),
                    30f - 2f * dist);
            double force = 4.0 - 0.7 * dist;
            entity.push(-force * barrelVector.x, -force * barrelVector.y, -force * barrelVector.z);
        }

        // ---- rearward exhaust plume ----
        // getShootPos gives the MUZZLE of the tube that just fired; step back one tube
        // length to reach its breech, where the blast actually vents from.
        Vec3 muzzle = self.getShootPos(turretIndex, 1f);
        Vec3 breech = muzzle.add(back.scale(FCP_TUBE_LENGTH));

        // --- FCP muzzle flash blasting out the rear of the tube ---
        // Uses FCP's own muzzle particles. Two things matter here:
        //   1. SIZE_UNIT = 0.2, so scale 10 ~= 2 blocks. A scale of 1 is a 20cm speck.
        //      FCP's own TANK preset peaks at base 10.5; a Grad backblast is bigger still.
        //   2. Blooms run base -> target, i.e. they START large and COLLAPSE, exactly as
        //      spawnBloom() does. Growing from small looks like nothing at all.
        // Sent via level.sendParticles with count 0, so the xyz args are a VELOCITY
        // vector rather than a spread - same as FCPMuzzleEffects.send().
        RandomSource rand = self.getRandom();

        // Fireball: three collapsing blooms staggered backwards, so the plume is a jet
        // running away from the tubes rather than a ball sat on them.
        // (r, g, b, life, fade, animSpeed, baseScale, targetScale, frameCount, layer)
        Vec3 b0 = breech;
        level.sendParticles(
                new FCPMuzzleParticleOption(1f, 1f, 0.95f, 8, 0.86f, 1, 16f, 1.5f, 1,
                        FCPMuzzleParticleOption.LAYER_BLOOM),
                b0.x, b0.y, b0.z, 0, 0.0, 0.0, 0.0, 1.0);          // ~3.2 blocks, white hot

        Vec3 b1 = breech.add(back.scale(1.0));
        level.sendParticles(
                new FCPMuzzleParticleOption(1f, 0.90f, 0.55f, 9, 0.87f, 1, 13f, 1.2f, 1,
                        FCPMuzzleParticleOption.LAYER_BLOOM),
                b1.x, b1.y, b1.z, 0, 0.0, 0.0, 0.0, 1.0);          // ~2.6 blocks, yellow

        Vec3 b2 = breech.add(back.scale(2.2));
        level.sendParticles(
                new FCPMuzzleParticleOption(1f, 0.62f, 0.28f, 10, 0.88f, 1, 9f, 1.0f, 1,
                        FCPMuzzleParticleOption.LAYER_BLOOM),
                b2.x, b2.y, b2.z, 0, 0.0, 0.0, 0.0, 1.0);          // ~1.8 blocks, orange

        // Animated flash frames on the tube mouth itself.
        level.sendParticles(
                new FCPMuzzleParticleOption(1f, 1f, 1f, 12, 0.88f, 2, 6f, 8f, 9,
                        FCPMuzzleParticleOption.LAYER_BANG_STATIC),
                breech.x, breech.y, breech.z, 0, 0.0, 0.0, 0.0, 1.0);

        // Sparks thrown out the back.
        for (int i = 0; i < 12; i++) {
            Vec3 jet = back.scale(0.35 + rand.nextDouble() * 0.45).add(
                    (rand.nextDouble() - 0.5) * 0.18,
                    (rand.nextDouble() - 0.5) * 0.18,
                    (rand.nextDouble() - 0.5) * 0.18);
            level.sendParticles(
                    new FCPMuzzleParticleOption(1f, 0.95f, 0.75f, 8, 0.86f, 1, 2.6f, 0.02f, 9,
                            FCPMuzzleParticleOption.LAYER_BANG_SPARK),
                    breech.x, breech.y, breech.z, 0, jet.x, jet.y, jet.z, 1.0);
        }

        // --- smoke plume, thinner and tighter than before ---
        // Same particle MediumRocketEntity trails (largeTrail -> CAMPFIRE_COSY_SMOKE).
        Vec3 core = breech.add(back.scale(0.5));
        ParticleTool.sendParticle(level, ParticleTypes.CAMPFIRE_COSY_SMOKE,
                core.x, core.y, core.z,
                10, 0.22, 0.22, 0.22, 0.01, true);

        Vec3 mid = breech.add(back.scale(1.5));
        ParticleTool.sendParticle(level, ParticleTypes.CAMPFIRE_COSY_SMOKE,
                mid.x, mid.y, mid.z,
                6, 0.35, 0.35, 0.35, 0.015, true);

        Vec3 tail = breech.add(back.scale(2.6));
        ParticleTool.sendParticle(level, ParticleTypes.CAMPFIRE_COSY_SMOKE,
                tail.x, tail.y, tail.z,
                4, 0.5, 0.5, 0.5, 0.02, true);
    }
}