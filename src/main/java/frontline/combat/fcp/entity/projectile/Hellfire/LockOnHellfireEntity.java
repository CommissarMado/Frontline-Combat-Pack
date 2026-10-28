package frontline.combat.fcp.entity.projectile.Hellfire;

import com.atsuishio.superbwarfare.config.server.ExplosionConfig;
import com.atsuishio.superbwarfare.entity.projectile.MissileProjectile;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.init.ModSounds;
import com.atsuishio.superbwarfare.init.ModTags;
import com.atsuishio.superbwarfare.tools.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import org.joml.Math;

import java.util.List;

public class LockOnHellfireEntity extends MissileProjectile implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** Steering rate, degrees per tick after the motor lights: ramps up, then caps here (9M123-like). */
    private static final float MAX_TURN = 1.5f, TURN_RAMP = 0.15f;
    /** Seeker half-angle (degrees) and how long (ticks) the target may hide behind blocks. */
    private static final double SEEKER_CONE = 20;
    private static final int HIDDEN_GRACE = 10;

    private int hiddenTicks;

    public LockOnHellfireEntity(EntityType<? extends LockOnHellfireEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
        // Same as the weapon config, in case the shot doesn't overwrite them.
        setDamage(175f);
        setExplosionDamage(19.4f);
        setExplosionRadius(6);
        this.setDistracted(false);
    }

    private boolean seekerLost(Vec3 aim) {
        Vec3 toTarget = position().vectorTo(aim);
        if (VectorTool.calculateAngle(getLookAngle(), toTarget) > SEEKER_CONE) return true;
        boolean blocked = level().clip(new ClipContext(position(), aim, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this))
                .getType() != HitResult.Type.MISS;
        hiddenTicks = blocked ? hiddenTicks + 1 : 0;
        return hiddenTicks > HIDDEN_GRACE;
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return ModItems.LARGE_ANTI_GROUND_MISSILE.get();
    }

    @Override
    public void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        if (entity == this.getOwner() || (this.getOwner() != null && entity == this.getOwner().getVehicle()) || entity instanceof LockOnHellfireEntity)
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

    private void explodeAndDiscard() {
        if (this.level() instanceof ServerLevel) {
            ProjectileTool.causeCustomExplode(this,
                    ModDamageTypes.causeProjectileExplosionDamage(this.level().registryAccess(), this, this.getOwner()),
                    this, getExplosionDamage(), getExplosionRadius());
        }
        this.discard();
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
            explodeAndDiscard();
        }
    }
    @Override
    public void tick() {
        super.tick();

        largeTrail();

        Entity entity = EntityFindUtil.findEntity(this.level(), entityData.get(TARGET_UUID));
        List<Entity> decoy = SeekTool.seekLivingEntities(this, 32, 90);

        for (var e : decoy) {
            if (e.getType().is(ModTags.EntityTypes.DECOY) && !this.isDistracted()) {
                this.entityData.set(TARGET_UUID, e.getStringUUID());
                this.setDistracted(true);
                break;
            }
        }

        Vec3 toVec = null;

        // Seeker like the 9M123's: flies straight at the target (no top-attack climb), and a target that
        // leaves the seeker cone or stays behind cover past the grace period is lost for good.
        if (getGuideType() == 0) {
            if (!entityData.get(TARGET_UUID).equals("none") && entity != null && level() instanceof ServerLevel) {
                Vec3 aim = entity.getBoundingBox().getCenter();
                if (tickCount > 8 && seekerLost(aim)) {
                    this.entityData.set(TARGET_UUID, "none");
                } else {
                    if ((!entity.getPassengers().isEmpty() || entity instanceof VehicleEntity) && entity.tickCount % ((int) Math.max(0.04 * this.distanceTo(entity), 2)) == 0) {
                        entity.level().playSound(null, entity.getOnPos(), entity instanceof Pig ? SoundEvents.PIG_HURT : ModSounds.MISSILE_WARNING.get(), SoundSource.PLAYERS, 2, 1f);
                    }
                    toVec = RangeTool.calculateFiringSolution(position(), aim, entity.getDeltaMovement(), getDeltaMovement().length(), 0);
                }
            }
        } else {
            if (level() instanceof ServerLevel) {
                double dis = getTargetPos().vectorTo(position()).horizontalDistance();
                double height = dis > 30 ? 0.4 * (dis - 30) : 0;
                Vec3 targetPos = getTargetPos().add(0, height, 0);
                toVec = RangeTool.calculateFiringSolution(position(), targetPos, Vec3.ZERO, getDeltaMovement().length(), 0);
            }
        }

        if (this.tickCount > 8) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.05).add(getLookAngle().scale(8)));
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.85, 0.85, 0.85));
            if (toVec != null) {
                turn(toVec, Mth.clamp((tickCount - 8) * TURN_RAMP, 0, MAX_TURN));
            }
        } else {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.06, 0));
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.99, 0.99, 0.99));
        }

        if (this.tickCount == 8) {
            this.level().playSound(null, BlockPos.containing(position()), ModSounds.MISSILE_START.get(), SoundSource.PLAYERS, 4, 1);
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                ParticleTool.sendParticle(serverLevel, ParticleTypes.CLOUD, this.xo, this.yo, this.zo, 15, 0.8, 0.8, 0.8, 0.01, true);
                ParticleTool.sendParticle(serverLevel, ParticleTypes.CAMPFIRE_COSY_SMOKE, this.xo, this.yo, this.zo, 10, 0.8, 0.8, 0.8, 0.01, true);
            }
        }
    }

    private PlayState movementPredicate(AnimationState<LockOnHellfireEntity> event) {
        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.jvm.idle"));
    }

    @Override
    public float getGravity() {
        return tickCount < 8 ? 0.15F : super.getGravity();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 0, this::movementPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    @Override
    public @NotNull SoundEvent getSound() {
        return ModSounds.ROCKET_FLY.get();
    }

    @Override
    public float getVolume() {
        return 0.7f;
    }

    @Override
    public float getMaxHealth() {
        return 70;
    }
}
