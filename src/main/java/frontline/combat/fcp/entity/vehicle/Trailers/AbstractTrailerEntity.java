package frontline.combat.fcp.entity.vehicle.Trailers;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import frontline.combat.fcp.init.ModItems;
import frontline.combat.fcp.init.TrailerDriverConfigs;
import frontline.combat.fcp.init.TrailerTowedConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * AbstractTrailerEntity — base for every towed trailer in FCP.
 *
 * ── Design ───────────────────────────────────────────────────────────────────
 * A trailer is an SBW vehicle with an "Empty" engine, so SBW applies no driving
 * forces of its own. The hitch is a TWO-POINT constraint solved server-side every
 * tick: the trailer's tongue point is pinned to the driver's hitch point, and the
 * trailer body trails and rotates around that pin.
 *
 *   driver hitch point   ← from TrailerDriverData  (data/<ns>/trailer_driver/<driver>.json)
 *   trailer tongue point ← from TrailerTowedData   (data/<ns>/trailer_towed/<trailer>.json)
 *
 * Each tick (server, while attached):
 *   1. Hitch world position H = driver pos + R(driverYaw) · driverHitchOffset
 *   2. New trailer yaw φ = heading from the trailer's CURRENT body position to H
 *      (this is what makes it trail naturally), clamped to ±max_articulation of
 *      the driver's heading to stop violent jackknife flips.
 *   3. New trailer position so the tongue lands exactly on H:
 *         pos = H − R(φ) · trailerTongueOffset
 *   4. setPos / setYRot — NOT velocity. (SBW clamps large velocity changes, so a
 *      velocity-driven follower lags and jitters; positional solving sidesteps that.)
 *
 * The server is authoritative; with setUpdateInterval(1) the position is synced
 * every tick and vanilla/SBW interpolation renders it smoothly on the client, so
 * there is no client-side follow code and no custom yaw sync.
 *
 * ── Local → world transform ──────────────────────────────────────────────────
 * SBW renders/positions with Axis.YP.rotationDegrees(-yaw), so the matching
 * local→world rotation for a yaw θ (radians) is:
 *   worldX = x + (lx·cosθ − lz·sinθ)
 *   worldZ = z + (lx·sinθ + lz·cosθ)
 * Local +Z is forward. This is the convention used throughout this class.
 *
 * ── Subclass contract ────────────────────────────────────────────────────────
 * Concrete trailers only implement camo + GeckoLib; the tongue/whitelist all come
 * from the trailer's trailer_towed JSON, resolved automatically by registry id.
 */
public abstract class AbstractTrailerEntity extends CamoVehicleBase {

    // ── Synced data (minimal; server authoritative) ──────────────────────────
    private static final EntityDataAccessor<Optional<UUID>> DRIVER_UUID =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> ATTACHED =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final double ATTACH_SEARCH_RADIUS = 6.0;

    @Nullable
    private Entity cachedDriver;

    protected AbstractTrailerEntity(EntityType<? extends GeoVehicleEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DRIVER_UUID, Optional.empty());
        this.entityData.define(ATTACHED, false);
    }

    /** Tongue / tow rules for THIS trailer, resolved from its registry id. */
    @Nullable
    public TrailerTowedData getTowedData() {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(this.getType());
        return id == null ? null : TrailerTowedConfigs.get(id);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void baseTick() {
        // Capture last tick's yaw as the interpolation origin BEFORE SBW runs, so
        // the client lerps smoothly from the previous constraint yaw to the new one.
        if (isAttached()) {
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        // Run the full SBW lifecycle (handles damage, wreck, camo, client sync,
        // gravity + move). For an Empty-engine trailer there are no driving forces.
        super.baseTick();

        // Constraint solving is server-authoritative only; the client just renders
        // the synced position with normal interpolation.
        if (this.level().isClientSide()) return;
        if (!isAttached()) return;

        Entity driver = resolveDriver();
        if (driver == null) return; // resolveDriver() already detaches if gone

        applyHitchConstraint(driver);
    }

    private void applyHitchConstraint(Entity driver) {
        TrailerTowedData towed = getTowedData();
        if (towed == null) return;

        ResourceLocation driverId = ForgeRegistries.ENTITY_TYPES.getKey(driver.getType());
        if (driverId == null) return;
        TrailerDriverData drv = TrailerDriverConfigs.get(driverId);
        if (drv == null) return;

        // Tick-order compensation. Entities tick in an arbitrary order, so on any
        // given tick the driver may not have moved yet when this runs. If it hasn't
        // (its tickCount is still behind ours), its reported position is last tick's
        // and pinning to it would leave the trailer one tick behind — a gap that
        // scales with speed and grows under acceleration. Anticipate the driver's
        // horizontal movement by its current velocity to cancel that lag. When the
        // driver has already ticked, its position is current and no anticipation is
        // applied. Vertical is left to hitch_y so gravity never enters the estimate.
        double antX = 0.0, antZ = 0.0;
        if (driver.tickCount < this.tickCount) {
            net.minecraft.world.phys.Vec3 dv = driver.getDeltaMovement();
            antX = dv.x;
            antZ = dv.z;
        }
        double driverX = driver.getX() + antX;
        double driverZ = driver.getZ() + antZ;
        double driverY = driver.getY();

        // 1. Hitch world position from the driver.
        double thetaD = Math.toRadians(driver.getYRot());
        double cosD = Math.cos(thetaD), sinD = Math.sin(thetaD);
        double hx = driverX + (drv.hitchX() * cosD - drv.hitchZ() * sinD);
        double hz = driverZ + (drv.hitchX() * sinD + drv.hitchZ() * cosD);
        double hy = driverY + drv.hitchY();

        // 2. Trailing yaw: aim the trailer's front from its CURRENT body to the hitch.
        double dx = hx - this.getX();
        double dz = hz - this.getZ();
        float yaw;
        if (dx * dx + dz * dz < 1.0e-6) {
            yaw = this.getYRot();
        } else {
            yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        }

        // Clamp articulation to ±max_articulation of the driver's heading.
        float rel = Mth.wrapDegrees(yaw - driver.getYRot());
        float maxArt = towed.maxArticulation();
        if (rel > maxArt) rel = maxArt;
        if (rel < -maxArt) rel = -maxArt;
        yaw = Mth.wrapDegrees(driver.getYRot() + rel);

        // 3. Position so the tongue lands exactly on the hitch point.
        double thetaT = Math.toRadians(yaw);
        double cosT = Math.cos(thetaT), sinT = Math.sin(thetaT);
        double newX = hx - (towed.towX() * cosT - towed.towZ() * sinT);
        double newZ = hz - (towed.towX() * sinT + towed.towZ() * cosT);
        double newY = hy - towed.towY();

        // 4. Apply by position, not velocity (avoids SBW's acceleration clamp).
        this.setPos(newX, newY, newZ);
        this.setYRot(yaw);
        this.setXRot(0.0f);
        this.setDeltaMovement(Vec3.ZERO);
    }

    // ── Attach / detach API ───────────────────────────────────────────────────

    /**
     * Attempts to hitch this trailer to the given driver vehicle.
     * Returns false if the driver has no hitch point, this trailer has no tow
     * config, or the driver is not on this trailer's whitelist.
     */
    public boolean attach(Entity driver) {
        if (driver == null) return false;

        ResourceLocation driverId = ForgeRegistries.ENTITY_TYPES.getKey(driver.getType());
        if (driverId == null) return false;

        TrailerDriverData drv = TrailerDriverConfigs.get(driverId);
        if (drv == null) return false;                 // driver can't tow

        TrailerTowedData towed = getTowedData();
        if (towed == null) return false;               // trailer has no tow config
        if (!towed.canBeTowedBy(driverId)) return false; // not whitelisted

        this.entityData.set(DRIVER_UUID, Optional.of(driver.getUUID()));
        this.entityData.set(ATTACHED, true);
        this.cachedDriver = driver;

        // One-time snap: face the driver's heading, tongue on the hitch.
        double thetaD = Math.toRadians(driver.getYRot());
        double cosD = Math.cos(thetaD), sinD = Math.sin(thetaD);
        double hx = driver.getX() + (drv.hitchX() * cosD - drv.hitchZ() * sinD);
        double hz = driver.getZ() + (drv.hitchX() * sinD + drv.hitchZ() * cosD);
        double hy = driver.getY() + drv.hitchY();

        float yaw = driver.getYRot();
        double cosT = Math.cos(thetaD), sinT = Math.sin(thetaD); // same as driver yaw on snap
        double newX = hx - (towed.towX() * cosT - towed.towZ() * sinT);
        double newZ = hz - (towed.towX() * sinT + towed.towZ() * cosT);
        double newY = hy - towed.towY();

        this.setPos(newX, newY, newZ);
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.setXRot(0.0f);
        this.setDeltaMovement(Vec3.ZERO);
        return true;
    }

    /** Releases the trailer; SBW physics (gravity, settling) resume next tick. */
    public void detach() {
        this.entityData.set(DRIVER_UUID, Optional.empty());
        this.entityData.set(ATTACHED, false);
        this.cachedDriver = null;
    }

    public boolean isAttached() {
        return this.entityData.get(ATTACHED);
    }

    @Nullable
    public Entity getDriver() {
        return resolveDriver();
    }

    @Nullable
    private Entity resolveDriver() {
        Optional<UUID> id = this.entityData.get(DRIVER_UUID);
        if (id.isEmpty()) return null;

        if (cachedDriver != null && cachedDriver.isAlive()
                && cachedDriver.getUUID().equals(id.get())) {
            return cachedDriver;
        }

        if (this.level() instanceof ServerLevel sl) {
            cachedDriver = sl.getEntity(id.get());
            if (cachedDriver == null) {
                detach(); // driver despawned / unloaded
            }
            return cachedDriver;
        }
        return null; // client doesn't solve the constraint
    }

    @Nullable
    private Entity findNearestDriver() {
        TrailerTowedData towed = getTowedData();
        if (towed == null) return null;

        List<Entity> candidates = this.level().getEntities(
                this,
                this.getBoundingBox().inflate(ATTACH_SEARCH_RADIUS),
                entity -> {
                    if (entity == this) return false;
                    if (!(entity instanceof GeoVehicleEntity)) return false;
                    if (entity instanceof AbstractTrailerEntity) return false; // no chaining yet
                    if (entity instanceof Player) return false;
                    ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
                    return id != null
                            && TrailerDriverConfigs.has(id)
                            && towed.canBeTowedBy(id);
                }
        );

        if (candidates.isEmpty()) return null;
        return candidates.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(this.position())))
                .orElse(null);
    }

    // ── Interaction (single source of truth for attach/detach) ────────────────

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        // Spray can → camo cycling, handled by CamoVehicleBase.
        if (player.getItemInHand(hand).is(ModItems.SPRAY.get())) {
            return super.interact(player, hand);
        }

        if (this.level().isClientSide()) return InteractionResult.SUCCESS;

        // Sneak + right-click always detaches.
        if (player.isShiftKeyDown()) {
            if (isAttached()) { detach(); say(player, "fcp.trailer.detached"); }
            else say(player, "fcp.trailer.not_attached");
            return InteractionResult.SUCCESS;
        }

        if (isAttached()) {
            detach();
            say(player, "fcp.trailer.detached");
            return InteractionResult.SUCCESS;
        }

        Entity driver = findNearestDriver();
        if (driver == null) {
            say(player, "fcp.trailer.no_vehicle_nearby");
            return InteractionResult.SUCCESS;
        }

        if (attach(driver)) say(player, "fcp.trailer.attached");
        else say(player, "fcp.trailer.cannot_attach");
        return InteractionResult.SUCCESS;
    }

    private static void say(Player player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }

    // ── Behaviour while hitched ───────────────────────────────────────────────

    @Override
    public boolean canCollideWith(Entity other) {
        if (isAttached()) {
            Optional<UUID> id = this.entityData.get(DRIVER_UUID);
            if (id.isPresent() && other.getUUID().equals(id.get())) return false;
        }
        return super.canCollideWith(other);
    }

    @Override
    public boolean isPushable() {
        return !isAttached() && super.isPushable();
    }

    @Override
    public void push(Entity other) {
        if (isAttached()) return;
        super.push(other);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (!isAttached()) {
            super.checkFallDamage(y, onGround, state, pos);
        }
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.entityData.get(DRIVER_UUID).ifPresent(id -> compound.putUUID("TrailerDriverUUID", id));
        compound.putBoolean("TrailerAttached", isAttached());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("TrailerDriverUUID")) {
            this.entityData.set(DRIVER_UUID, Optional.of(compound.getUUID("TrailerDriverUUID")));
        }
        if (compound.contains("TrailerAttached")) {
            this.entityData.set(ATTACHED, compound.getBoolean("TrailerAttached"));
        }
    }
}