package frontline.combat.fcp.entity.projectile.Khrizantema;

import com.atsuishio.superbwarfare.config.server.ExplosionConfig;
import com.atsuishio.superbwarfare.entity.projectile.MissileProjectile;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.init.ModSounds;
import com.atsuishio.superbwarfare.init.ModTags;
import com.atsuishio.superbwarfare.tools.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The Khrizantema's radar-guided 9M123: flies like SBW's wire-guided missile (same speed build-up and
 * smoke trail, no drop before the motor lights) but homes on the target locked by the seeker, flying
 * straight at it - no top-attack climb. It turns slowly ({@link #MAX_TURN} degrees a tick) and its seeker
 * only sees {@link #SEEKER_CONE} degrees either side of its nose: a target that gets outside that, or
 * stays behind cover past a short grace period, is lost for good and the missile flies on straight.
 * Drawn as SBW's wire-guided missile by LockOn9M123Renderer.
 */
public class LockOn9M123Entity extends MissileProjectile {

    /** Steering rate, degrees per tick: ramps up over the first ticks, then caps here. */
    private static final float MAX_TURN = 1.0f, TURN_RAMP = 0.1f;
    /** Seeker half-angle (degrees) and how long (ticks) the target may hide behind blocks. */
    private static final double SEEKER_CONE = 20;
    private static final int HIDDEN_GRACE = 10;

    private int hiddenTicks;

    public LockOn9M123Entity(EntityType<? extends LockOn9M123Entity> type, Level level) {
        super(type, level);
        this.noCulling = true;
        // Same as the weapon config, in case the shot doesn't overwrite them.
        setDamage(175f);
        setExplosionDamage(17f);
        setExplosionRadius(5);
        this.setDistracted(false);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return ModItems.MEDIUM_ANTI_GROUND_MISSILE.get();
    }

    @Override
    public void tick() {
        super.tick();
        mediumTrail();

        Entity target = EntityFindUtil.findEntity(this.level(), entityData.get(TARGET_UUID));
        List<Entity> decoys = SeekTool.seekLivingEntities(this, 32, 90);
        for (var e : decoys) {
            if (e.getType().is(ModTags.EntityTypes.DECOY) && !this.isDistracted()) {
                this.entityData.set(TARGET_UUID, e.getStringUUID());
                this.setDistracted(true);
                break;
            }
        }

        // Same motor as SBW's wire-guided missile: settles at 4 blocks a tick along its heading.
        this.setDeltaMovement(this.getDeltaMovement().scale(0.5).add(getLookAngle().scale(2.0)));

        Vec3 toVec = null;
        if (level() instanceof ServerLevel) {
            if (getGuideType() == 0) {
                if (!entityData.get(TARGET_UUID).equals("none") && target != null) {
                    Vec3 aim = target.getBoundingBox().getCenter();
                    if (seekerLost(aim)) {
                        // Out of the seeker's view: the lock is gone for good, it flies on straight.
                        this.entityData.set(TARGET_UUID, "none");
                    } else {
                        if ((!target.getPassengers().isEmpty() || target instanceof VehicleEntity)
                                && target.tickCount % ((int) Math.max(0.04 * this.distanceTo(target), 2)) == 0) {
                            target.level().playSound(null, target.getOnPos(), target instanceof Pig ? SoundEvents.PIG_HURT : ModSounds.MISSILE_WARNING.get(),
                                    SoundSource.PLAYERS, 2, 1f);
                        }
                        toVec = RangeTool.calculateFiringSolution(position(), aim, target.getDeltaMovement(), getDeltaMovement().length(), 0);
                    }
                }
            } else {
                toVec = RangeTool.calculateFiringSolution(position(), getTargetPos(), Vec3.ZERO, getDeltaMovement().length(), 0);
            }
        }
        if (toVec != null && tickCount > 0) {
            turn(toVec, Mth.clamp(tickCount * TURN_RAMP, 0, MAX_TURN));
        }
    }

    /**
     * The seeker loses the target once it is outside its view cone, or hidden behind blocks for
     * longer than a short grace period.
     */
    private boolean seekerLost(Vec3 aim) {
        Vec3 toTarget = position().vectorTo(aim);
        if (VectorTool.calculateAngle(getLookAngle(), toTarget) > SEEKER_CONE) return true;
        boolean blocked = level().clip(new ClipContext(position(), aim, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this))
                .getType() != HitResult.Type.MISS;
        hiddenTicks = blocked ? hiddenTicks + 1 : 0;
        return hiddenTicks > HIDDEN_GRACE;
    }

    @Override
    public void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        if (entity == this.getOwner() || (this.getOwner() != null && entity == this.getOwner().getVehicle()) || entity instanceof LockOn9M123Entity)
            return;
        if (this.level() instanceof ServerLevel) {
            DamageHandler.doDamage(entity, ModDamageTypes.causeProjectileHitDamage(this.level().registryAccess(), this, this.getOwner()), getDamage());
            if (entity instanceof LivingEntity) {
                entity.invulnerableTime = 0;
            }
            causeExplode(result.getLocation());
            discard();
        }
    }

    @Override
    public void onHitBlock(@NotNull BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.level() instanceof ServerLevel) {
            BlockPos resultPos = blockHitResult.getBlockPos();
            float hardness = this.level().getBlockState(resultPos).getBlock().defaultDestroyTime();
            if (hardness != -1 && ExplosionConfig.EXPLOSION_DESTROY.get() && ExplosionConfig.EXTRA_EXPLOSION_EFFECT.get()) {
                this.level().destroyBlock(resultPos, true);
            }
            ProjectileTool.causeCustomExplode(this,
                    ModDamageTypes.causeProjectileExplosionDamage(this.level().registryAccess(), this, this.getOwner()),
                    this, getExplosionDamage(), getExplosionRadius());
            this.discard();
        }
    }

    @Override
    public @NotNull SoundEvent getSound() {
        return ModSounds.ROCKET_FLY.get();
    }

    @Override
    public float getVolume() {
        return 0.4f;
    }

    @Override
    public float getMaxHealth() {
        return 20;
    }
}
