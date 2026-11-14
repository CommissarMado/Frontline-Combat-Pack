package frontline.combat.fcp.entity.projectile;

import com.atsuishio.superbwarfare.config.server.ExplosionConfig;
import com.atsuishio.superbwarfare.entity.projectile.Tm62Entity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.tools.CustomExplosion;
import frontline.combat.fcp.FcpWorldSettings;
import frontline.combat.fcp.init.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * fcp:mine (user, 2026-11-03): a copy of Squad's TM-62 anti-tank mine - SBW's TM-62 model and renderer, but Squad's
 * behaviour: a vehicle rolling onto it makes it click (Squad's mine click, fcp:squad.mine.click) and it goes off
 * {@link #DETONATION_DELAY} ticks later, so a fast vehicle can get out of the full-damage zone. Damage to vehicles is
 * Squad's AT mine (BP_Deployable_GenericMine_AT, inherited by the TM-62): 800 explosive inside 5 m, falling linearly to
 * 250 at 10 m (x0.097 FCP scale), armour ignored, x the vehicle's explosive modifier (VehicleArmor, armour model on).
 * Infantry and blocks get SBW's TM-62 blast. With the armour model off it is SBW's TM-62 blast with the delay.
 * SBW's own TM-62 is left untouched.
 */
public class FcpMineEntity extends Tm62Entity {

    /** Squad's MineDetonationDelay is 0.1 s; the user wants 0.5 s so light vehicles can outrun the blast. */
    public static final int DETONATION_DELAY = 10;
    /** Squad AT mine (FCP scale). */
    public static final float DAMAGE = 800f * 0.097f, MIN_DAMAGE = 250f * 0.097f;
    public static final double INNER_RADIUS = 5, OUTER_RADIUS = 10;
    private static final ResourceLocation CLICK = ResourceLocation.fromNamespaceAndPath("fcp", "squad.mine.click");

    /** Ticks left until it goes off once triggered; -1 while armed and waiting. */
    private int fuse = -1;
    /** True while this mine applies its Squad damage to the vehicles around it (VehicleArmor lets only those through). */
    private boolean squadHit;

    public FcpMineEntity(EntityType<? extends Tm62Entity> type, Level level) {
        super(castType(type), level);
    }

    @SuppressWarnings("unchecked")
    private static EntityType<Tm62Entity> castType(EntityType<? extends Tm62Entity> type) {
        return (EntityType<Tm62Entity>) type;
    }

    public boolean squadHit() {
        return squadHit;
    }

    @Override
    public void tick() {
        // SBW's Tm62Entity.tick (fall, settle, slide) without its instant detonation; Entity.tick() is just this.
        baseTick();
        setDeltaMovement(getDeltaMovement().add(0, -0.03, 0));
        if (!level().noCollision(getBoundingBox())) moveTowardsClosestSpace(getX(), (getBoundingBox().minY + getBoundingBox().maxY) / 2, getZ());
        move(MoverType.SELF, getDeltaMovement());
        float friction = onGround() ? level().getBlockState(getBlockPosBelowThatAffectsMyMovement()).getFriction(level(), getBlockPosBelowThatAffectsMyMovement(), this) * 0.98f : 0.98f;
        setDeltaMovement(getDeltaMovement().multiply(friction, 0.98, friction));
        if (onGround()) setDeltaMovement(getDeltaMovement().multiply(1, -0.9, 1));
        refreshDimensions();

        if (!(level() instanceof ServerLevel server)) return;
        if (fuse < 0) {
            boolean destroyed = entityData.get(HEALTH) <= 0;
            if (destroyed) {
                explode(server);
            } else if (tickCount >= 20 && onGround() && vehicleOnTop()) {
                fuse = DETONATION_DELAY;
                SoundEvent click = SoundEvent.createVariableRangeEvent(CLICK);
                server.playSound(null, getX(), getY(), getZ(), click, SoundSource.BLOCKS, 1f, 1f);
            }
        } else if (fuse-- == 0) {
            explode(server);
        } else if (fuse % 4 == 0) {
            server.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.1, getZ(), 1, 0.05, 0.02, 0.05, 0.01);
        }
    }

    /** Squad AT mines go off under vehicles only. */
    private boolean vehicleOnTop() {
        return !level().getEntitiesOfClass(VehicleEntity.class, getBoundingBox().inflate(0.2), v -> !v.isRemoved()).isEmpty();
    }

    private void explode(ServerLevel server) {
        if (isRemoved()) return;
        if (FcpWorldSettings.current().armorModel()) {
            AABB area = getBoundingBox().inflate(OUTER_RADIUS);
            squadHit = true;
            try {
                for (VehicleEntity v : server.getEntitiesOfClass(VehicleEntity.class, area, v -> !v.isRemoved())) {
                    var profile = frontline.combat.fcp.balance.VehicleArmor.profile(v);
                    if (profile == null || !profile.model()) continue; // not on the armour model: SBW's blast below
                    float damage = damageAt(distanceTo(v.getBoundingBox()));
                    if (damage > 0) v.hurt(ModDamageTypes.causeCustomExplosionDamage(server.registryAccess(), this, getOwner()), damage);
                }
            } finally {
                squadHit = false;
            }
        }
        new CustomExplosion.Builder(this).attacker(getOwner())
                .damage(ExplosionConfig.TM_62_EXPLOSION_DAMAGE.get())
                .radius(ExplosionConfig.TM_62_EXPLOSION_RADIUS.get())
                .explode();
        discard();
    }

    /** Squad's falloff: full damage inside 5 m, linear to the minimum at 10 m, nothing beyond. */
    public static float damageAt(double distance) {
        if (distance > OUTER_RADIUS) return 0;
        if (distance <= INNER_RADIUS) return DAMAGE;
        return (float) Mth.lerp((distance - INNER_RADIUS) / (OUTER_RADIUS - INNER_RADIUS), DAMAGE, MIN_DAMAGE);
    }

    private double distanceTo(AABB box) {
        Vec3 c = position();
        double dx = Math.max(Math.max(box.minX - c.x, 0), c.x - box.maxX), dy = Math.max(Math.max(box.minY - c.y, 0), c.y - box.maxY),
                dz = Math.max(Math.max(box.minZ - c.z, 0), c.z - box.maxZ);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /** Sneak-use by the owner picks it back up - as fcp:mine, not SBW's TM-62. */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (isOwnedBy(player) && player.isShiftKeyDown() && fuse < 0) {
            if (!level().isClientSide()) {
                discard();
                if (!player.getAbilities().instabuild) ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(ModItems.MINE.get()));
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FcpFuse", fuse);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("FcpFuse")) fuse = tag.getInt("FcpFuse");
    }
}
