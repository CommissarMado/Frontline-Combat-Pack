package frontline.combat.fcp.entity.projectile.Himars;

import com.atsuishio.superbwarfare.config.server.ExplosionConfig;
import com.atsuishio.superbwarfare.entity.projectile.MissileProjectile;
import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.init.ModSounds;
import com.atsuishio.superbwarfare.tools.CustomExplosion;
import com.atsuishio.superbwarfare.tools.DamageHandler;
import com.atsuishio.superbwarfare.tools.ParticleTool;
import com.atsuishio.superbwarfare.tools.RangeTool;
import frontline.combat.fcp.entity.vehicle.Himars.HimarsEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

/**
 * HIMARS GMLRS missile. Boosts straight for {@link #BOOST} ticks, then its motor lights and it steers, ramping up to a
 * limited turn rate:
 * <ul>
 *   <li>coordinate mode (SBW guide type 1, the entered position as target): climbs to its cruising height, just under
 *       the build limit (clear of any terrain, and it never goes higher), cruises towards the target, and dives
 *       onto it once it's close enough to pull down in time with its turning circle ({@link #MAX_TURN_COORD} degrees a
 *       tick). A target too close for that is flown at directly;</li>
 *   <li>TV mode: flown like a drone by the launching gunner (client: HimarsTvClient), turning as poorly as the
 *       Hellfires ({@link #MAX_TURN} degrees a tick). Their camera rides the missile and mouse look sets the heading it
 *       turns towards (HimarsTvAimPacket), for as long as they stay in that HIMARS's gunner seat; after that it flies
 *       straight on. While it flies, the pilot's chunks and entity tracking follow it (HimarsTvView), so the picture
 *       holds at any range.</li>
 * </ul>
 * Clients don't predict its flight: they ease towards the server's positions and take the exact heading from synced
 * data (vanilla rotation packets are coarse), so the TV picture doesn't stutter. The warhead goes off with SBW's
 * giant explosion effect.
 */
public class HimarsMissileEntity extends MissileProjectile implements GeoEntity {

    /** Entity id of the TV pilot (the launching gunner), or -1. */
    private static final EntityDataAccessor<Integer> PILOT = SynchedEntityData.defineId(HimarsMissileEntity.class, EntityDataSerializers.INT);
    /** The exact heading (yaw / pitch, look convention), for smooth client rendering and the TV camera. */
    private static final EntityDataAccessor<Float> HEADING_YAW = SynchedEntityData.defineId(HimarsMissileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEADING_PITCH = SynchedEntityData.defineId(HimarsMissileEntity.class, EntityDataSerializers.FLOAT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final int BOOST = 8;
    private static final float MAX_TURN = 1.5f, MAX_TURN_COORD = 2.5f, TURN_RAMP = 0.15f;
    /**
     * Coordinate mode: the dive starts this many blocks (horizontally) further out than the height to lose - room for
     * the turn from level flight into the dive. While cruising it steers for a point this far ahead.
     */
    private static final double DIVE_LEAD = 70, CRUISE_LOOKAHEAD = 150;
    /** Client: share of the gap to the server position closed each tick. */
    private static final double CLIENT_EASE = 0.5;

    private @Nullable UUID launcher;
    /** TV mode: the heading the pilot is steering for (yaw / pitch, look convention). */
    private float aimYaw, aimPitch;
    /** Client: the latest position from the server, eased towards each tick. */
    private @Nullable Vec3 serverPos;

    public HimarsMissileEntity(EntityType<? extends HimarsMissileEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.setDistracted(false);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PILOT, -1);
        this.entityData.define(HEADING_YAW, Float.NaN);
        this.entityData.define(HEADING_PITCH, Float.NaN);
    }

    // TEMPORARY diagnostics for the TV-return bug: why and where the missile goes on each side.
    @Override
    public void remove(RemovalReason reason) {
        com.mojang.logging.LogUtils.getLogger().info("[HIMARS-DBG] MISSILE {} removed: id={} reason={} t={} pos={} pilot={}",
                level().isClientSide() ? "CLIENT" : "SERVER", getId(), reason, tickCount,
                String.format("%.0f,%.0f,%.0f", getX(), getY(), getZ()), pilotId());
        super.remove(reason);
    }

    /** Client: keep ticking (and easing) even before the chunk it has flown into reaches the TV pilot. */
    @Override
    public boolean isAlwaysTicking() {
        return level() != null && level().isClientSide();
    }

    /** Client: remember the server's position instead of snapping to it; tick() eases towards it. */
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        serverPos = new Vec3(x, y, z);
    }

    /** Entity id of the player flying this missile, or -1. */
    public int pilotId() {
        return this.entityData.get(PILOT);
    }

    /** From HimarsTvAimPacket: only the missile's own pilot steers it. */
    public void setPilotAim(ServerPlayer player, float yaw, float pitch) {
        if (player.getId() != pilotId()) return;
        aimYaw = yaw;
        aimPitch = Mth.clamp(pitch, -90f, 90f);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return ModItems.EXTRA_LARGE_ANTI_GROUND_MISSILE.get();
    }

    @Override
    public void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        if (entity == getOwner() || (getOwner() != null && entity == getOwner().getVehicle()) || entity instanceof HimarsMissileEntity) return;
        if (level() instanceof ServerLevel) {
            DamageHandler.doDamage(entity, ModDamageTypes.causeProjectileHitDamage(level().registryAccess(), this, getOwner()), getDamage());
            if (entity instanceof LivingEntity) entity.invulnerableTime = 0;
            explode(result.getLocation());
            discard();
        }
    }

    @Override
    public void onHitBlock(@NotNull BlockHitResult hit) {
        super.onHitBlock(hit);
        if (level() instanceof ServerLevel) {
            BlockPos pos = hit.getBlockPos();
            if (level().getBlockState(pos).getBlock().defaultDestroyTime() != -1 && ExplosionConfig.EXPLOSION_DESTROY.get()
                    && ExplosionConfig.EXTRA_EXPLOSION_EFFECT.get()) {
                level().destroyBlock(pos, true);
            }
            explode(hit.getLocation());
            discard();
        }
    }

    /** The warhead's blast, with SBW's giant explosion particles (a step above what its 15-block radius would get). */
    private void explode(Vec3 pos) {
        new CustomExplosion.Builder(this)
                .damageSource(ModDamageTypes.causeProjectileExplosionDamage(level().registryAccess(), this, getOwner()))
                .attacker(getOwner())
                .damage(getExplosionDamage())
                .radius(getExplosionRadius())
                .position(pos)
                .withParticleType(ParticleTool.ParticleType.GIANT)
                .explode();
    }

    private void dropPilot() {
        if (pilotId() >= 0) this.entityData.set(PILOT, -1);
    }

    /**
     * TV steering: the pilot's heading, or null once they've left the launching HIMARS's gunner seat. The launching
     * gunner becomes the pilot on the first ticks of flight.
     */
    private @Nullable Vec3 tvHeading() {
        Entity owner = getOwner();
        if (!(owner instanceof Player) || !(owner.getVehicle() instanceof HimarsEntity himars)) {
            dropPilot();
            return null;
        }
        if (launcher == null) {
            if (tickCount > 5) return null;
            launcher = himars.getUUID();
            aimYaw = getYRot();
            aimPitch = getXRot();
            this.entityData.set(PILOT, owner.getId());
            himars.setTvMissile(this);
        }
        if (pilotId() < 0) return null;
        if (!launcher.equals(himars.getUUID()) || himars.getSeatIndex(owner) != himars.getTurretControllerIndex()) {
            dropPilot();
            return null;
        }
        return Vec3.directionFromRotation(aimPitch, aimYaw);
    }

    @Override
    public void tick() {
        // Client: the life from the HIMARS config only exists on the server; the client copy kept SBW's 400-tick
        // default and discarded itself 20 s into a TV flight. The server removes it when it really ends.
        if (level().isClientSide()) setLife(Integer.MAX_VALUE);
        super.tick();
        largeTrail();

        Vec3 toVec = null;
        float maxTurn = MAX_TURN;
        if (level() instanceof ServerLevel) {
            if (getGuideType() == 1) {
                // Coordinates: climb to and cruise at the ceiling, then dive in time to make the turn.
                Vec3 target = getTargetPos();
                Vec3 flat = new Vec3(target.x - getX(), 0, target.z - getZ());
                double dis = flat.length();
                Vec3 aim = target;
                if (dis > ceiling() - target.y + DIVE_LEAD) {
                    Vec3 ahead = position().add(flat.scale(Math.min(CRUISE_LOOKAHEAD, dis) / dis));
                    aim = new Vec3(ahead.x, ceiling(), ahead.z);
                }
                toVec = RangeTool.calculateFiringSolution(position(), aim, Vec3.ZERO, getDeltaMovement().length(), 0);
                maxTurn = MAX_TURN_COORD;
            } else {
                toVec = tvHeading();
            }
        }

        if (tickCount > BOOST) {
            setDeltaMovement(getDeltaMovement().scale(0.05).add(getLookAngle().scale(8)));
            setDeltaMovement(getDeltaMovement().multiply(0.85, 0.85, 0.85));
            if (toVec != null) turn(toVec, Mth.clamp((tickCount - BOOST) * TURN_RAMP, 0, maxTurn));
        } else {
            setDeltaMovement(getDeltaMovement().add(0, -0.03, 0).multiply(0.99, 0.99, 0.99));
        }

        if (level().isClientSide()) {
            // Ease towards the server's flight (from where it was before this tick) instead of predicting it.
            if (serverPos != null) setPos(new Vec3(xo, yo, zo).lerp(serverPos, CLIENT_EASE));
            float yaw = this.entityData.get(HEADING_YAW), pitch = this.entityData.get(HEADING_PITCH);
            if (!Float.isNaN(yaw) && !Float.isNaN(pitch)) {
                setYRot(yaw);
                setXRot(pitch);
            }
        } else {
            if (getGuideType() == 1) holdCeiling();
            this.entityData.set(HEADING_YAW, getYRot());
            this.entityData.set(HEADING_PITCH, getXRot());
        }

        if (tickCount == BOOST) {
            level().playSound(null, BlockPos.containing(position()), ModSounds.MISSILE_START.get(), SoundSource.PLAYERS, 4, 1);
            if (level() instanceof ServerLevel server) {
                ParticleTool.sendParticle(server, ParticleTypes.CLOUD, xo, yo, zo, 15, 0.8, 0.8, 0.8, 0.01, true);
                ParticleTool.sendParticle(server, ParticleTypes.CAMPFIRE_COSY_SMOKE, xo, yo, zo, 10, 0.8, 0.8, 0.8, 0.01, true);
            }
        }
    }

    /**
     * Coordinate mode cruising height, and the highest it flies: just under the build limit, clear of any terrain.
     * SBW only keeps a projectile's chunks loaded at or below the build limit; above it the missile froze at the edge
     * of loaded chunks.
     */
    private double ceiling() {
        return level().getMaxBuildHeight() - 2;
    }

    /** Coordinate mode: at the ceiling the missile levels off and can't climb any higher (TV mode has no ceiling). */
    private void holdCeiling() {
        double ceiling = ceiling();
        if (getY() > ceiling) setPos(getX(), ceiling, getZ());
        if (getY() >= ceiling - 0.5) {
            if (getXRot() < 0) setXRot(0);
            Vec3 v = getDeltaMovement();
            if (v.y > 0) setDeltaMovement(v.x, 0, v.z);
        }
    }

    @Override
    public float getGravity() {
        return tickCount < BOOST ? 0.15F : super.getGravity();
    }

    private PlayState movementPredicate(AnimationState<HimarsMissileEntity> event) {
        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.jvm.idle"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 0, this::movementPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public @NotNull SoundEvent getSound() {
        return ModSounds.ROCKET_FLY.get();
    }

    @Override
    public float getVolume() {
        return 0.8f;
    }

    @Override
    public float getMaxHealth() {
        return 70;
    }
}
