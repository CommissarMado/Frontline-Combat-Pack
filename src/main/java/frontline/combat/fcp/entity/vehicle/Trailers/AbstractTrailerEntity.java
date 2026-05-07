package frontline.combat.fcp.entity.vehicle.Trailer;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import frontline.combat.fcp.init.FcpTrailerConfigs;
import frontline.combat.fcp.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * AbstractTrailerEntity — base type for all towed trailer entities in FCP.
 *
 * Extends CamoVehicleBase so trailers inherit camo spray support, the SBW
 * damage system, GeckoLib rendering, and all standard vehicle behaviour.
 *
 * ── Physics model ────────────────────────────────────────────────────────────
 *
 * The trailer moves entirely through its own deltaMovement — there is no
 * setPos() override. Each tick, before SBW's own physics runs, we compute
 * the velocity the trailer needs and set it as deltaMovement. SBW then
 * integrates that velocity through its normal move() call, so collisions,
 * step-up, and terrain interaction all work naturally.
 *
 * Velocity is computed as follows:
 *
 *   1. Find the world-space hitch point on the towing vehicle (from JSON offset).
 *   2. Reconstruct the trailer's rear axle from its current position and yaw
 *      (double precision, no stored floats — eliminates sync-lag drift).
 *   3. Project the tower's velocity onto the axle→hitch direction.
 *      This is the component of the tower's motion that actually pulls the trailer.
 *   4. Add a constraint correction proportional to how far the axle→hitch
 *      distance deviates from trailerLength. This prevents drift accumulation
 *      under acceleration without any position teleporting.
 *   5. Set deltaMovement = towDirection * (projectedSpeed + correction).
 *   6. Derive trailer yaw from atan2(hitch - axle) so corners are tracked
 *      naturally with realistic off-tracking, not copied from the tower.
 *
 * Since the trailer has no driver or engine, SBW's baseTick() physics applies
 * only friction and gravity on top of our velocity — both are negligible or
 * desirable. The correction term in step 4 compensates for friction decay.
 *
 * ── Subclass contract ────────────────────────────────────────────────────────
 *
 *   getConfigId()                → ResourceLocation of the trailer's JSON config
 *   getCamoTextures()            → texture paths for camo variants
 *   getCamoNames()               → display names for camo variants
 *   registerControllers()        → GeckoLib animation controllers
 *   getAnimatableInstanceCache() → GeckoLib instance cache
 *
 * ── JSON config ──────────────────────────────────────────────────────────────
 *
 * File: data/<namespace>/trailers/<id>.json
 *
 * Hitch offset (in towing vehicle local space):
 *   offset_x = lateral      (+right of tower, -left)
 *   offset_y = vertical     (+above tower origin)
 *   offset_z = longitudinal (-behind tower, +in front)
 *
 * trailer_length = blocks from hitch pin to rear axle.
 *
 * ── Interaction ──────────────────────────────────────────────────────────────
 *
 *   Right-click              → attach to nearest SBW vehicle, or detach
 *   Sneak + right-click      → force detach
 *   Right-click with spray   → cycle camo (CamoVehicleBase)
 */
public abstract class AbstractTrailerEntity extends CamoVehicleBase {

    // ════════════════════════════════════════════════════════════════════════
    // Synced data
    // ════════════════════════════════════════════════════════════════════════

    private static final EntityDataAccessor<Optional<UUID>> TOWER_UUID =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final EntityDataAccessor<Boolean> IS_TOWED =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.BOOLEAN);

    /**
     * Trailer yaw in degrees, derived each tick from atan2(hitch - axle).
     * Synced for client-side renderer lerp. The axle itself is NOT stored —
     * it is reconstructed each tick from entity position + this yaw.
     */
    private static final EntityDataAccessor<Float> TRAILER_YAW =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.FLOAT);

    // ════════════════════════════════════════════════════════════════════════
    // Client lerp state — read by renderer
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Yaw from the previous tick, exposed so the renderer can lerp:
     *   Mth.rotLerp(partialTick, entity.prevTrailerYaw, entity.getTrailerYaw())
     */
    public float prevTrailerYaw;

    // ════════════════════════════════════════════════════════════════════════
    // Runtime state (not synced)
    // ════════════════════════════════════════════════════════════════════════

    @Nullable
    private Entity cachedTower;

    // ════════════════════════════════════════════════════════════════════════
    // Constructor
    // ════════════════════════════════════════════════════════════════════════

    protected AbstractTrailerEntity(EntityType<? extends GeoVehicleEntity> type, Level world) {
        super(type, world);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Subclass contract
    // ════════════════════════════════════════════════════════════════════════

    /**
     * ResourceLocation of this trailer's JSON config.
     * Example: new ResourceLocation("fcp", "example_trailer")
     * File at: data/fcp/trailers/example_trailer.json
     */
    protected abstract ResourceLocation getConfigId();

    public final TrailerConfig getConfig() {
        return FcpTrailerConfigs.get(getConfigId());
    }

    // ════════════════════════════════════════════════════════════════════════
    // Synced data setup
    // ════════════════════════════════════════════════════════════════════════

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TOWER_UUID, Optional.empty());
        this.entityData.define(IS_TOWED, false);
        this.entityData.define(TRAILER_YAW, 0.0f);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Tick
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void baseTick() {
        prevTrailerYaw = getTrailerYaw();

        Entity tower = resolveTower();

        if (tower != null) {
            // Set deltaMovement BEFORE super.baseTick() so that when SBW calls
            // move() internally, it integrates our kinematic velocity — not the
            // trailer's own engine physics (which is zero anyway, since there's
            // no driver). SBW then applies only friction and gravity on top,
            // both of which are handled gracefully by the correction term.
            applyTowedVelocity(tower);
        }

        // Always run SBW's full baseTick — handles move(), damage,
        // GeckoLib animations, and all other vehicle lifecycle work.
        super.baseTick();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Velocity-based kinematic model
    // ════════════════════════════════════════════════════════════════════════

    private void applyTowedVelocity(Entity tower) {
        TrailerConfig.HitchConfig hitch = getConfig().hitch();
        double trailerLength = getConfig().trailerLength();

        // ── 1. Compute world-space hitch point from tower ─────────────────
        //
        // Minecraft yaw: 0° = south (+Z), increases clockwise.
        //   worldX = towerX + (offsetX * cos) + (offsetZ * sin)
        //   worldZ = towerZ - (offsetX * sin) + (offsetZ * cos)
        double towerYawRad = Math.toRadians(tower.getYRot());
        double towerSin    = Math.sin(towerYawRad);
        double towerCos    = Math.cos(towerYawRad);

        double hitchX = tower.getX() + (hitch.offsetX() * towerCos) + (hitch.offsetZ() * towerSin);
        double hitchZ = tower.getZ() - (hitch.offsetX() * towerSin) + (hitch.offsetZ() * towerCos);

        // ── 2. Reconstruct axle from entity position and yaw ──────────────
        //
        // Derived from double-precision getX()/getZ() and synced yaw each tick.
        // Not stored as floats — eliminates precision drift and sync-packet lag.
        double halfLen       = trailerLength * 0.5;
        double trailerYawRad = Math.toRadians(this.getYRot());
        double trailerSin    = Math.sin(trailerYawRad);
        double trailerCos    = Math.cos(trailerYawRad);

        double axleX = this.getX() - trailerSin * halfLen;
        double axleZ = this.getZ() - trailerCos * halfLen;

        // ── 3. Compute axle→hitch vector ──────────────────────────────────
        double dx   = hitchX - axleX;
        double dz   = hitchZ - axleZ;
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist < 0.001) return; // degenerate — tower and trailer in same spot

        double nx = dx / dist; // normalized tow direction (axle → hitch)
        double nz = dz / dist;

        // ── 4. Project tower velocity onto the tow direction ──────────────
        //
        // Only the component of the tower's motion along the tow rod actually
        // pulls the trailer. The perpendicular component just makes the hitch
        // swing sideways, which is handled by the yaw derivation below.
        Vec3 towerVel = tower.getDeltaMovement();
        double projectedSpeed = towerVel.x * nx + towerVel.z * nz;

        // ── 5. Compute constraint correction ─────────────────────────────
        //
        // slack = how far the axle→hitch distance deviates from trailerLength.
        // A positive slack means the tower pulled ahead and the trailer is
        // stretching — we add velocity to close the gap.
        // A negative slack (compression) is unusual but handled gracefully.
        //
        // Correction factor 0.8: pulls the trailer firmly without oscillation.
        // Range: 0.5 (soft, slight lag) to 1.0 (stiff, instant correction).
        // Increase toward 1.0 if trailing lag is visible at high speeds.
        double slack      = dist - trailerLength;
        double correction = slack * 0.8;

        // ── 6. Compute and apply trailer velocity ─────────────────────────
        //
        // The trailer moves along the tow direction at the combined speed.
        // No setPos() — this velocity is what SBW's move() will integrate.
        double velX = nx * (projectedSpeed + correction);
        double velZ = nz * (projectedSpeed + correction);

        // Y velocity:
        //   - terrain_follow = true:  let move() handle step-up naturally.
        //     We allow downward velocity (falling off ledges) but not upward
        //     so we don't fight the terrain.
        //   - terrain_follow = false: inherit the tower's Y directly so the
        //     trailer rises/falls with bridges, ramps, etc.
        double velY = getConfig().terrainFollow()
                ? Math.min(this.getDeltaMovement().y, 0.0)
                : towerVel.y;

        this.setDeltaMovement(velX, velY, velZ);

        // ── 7. Derive trailer yaw from tow direction ──────────────────────
        //
        // Yaw is NOT copied from the tower. It is the direction the hitch
        // is relative to the axle — this produces natural off-tracking on
        // corners, where the axle lags behind the hitch's lateral movement.
        float newYaw = (float) Math.toDegrees(Math.atan2(dx, dz));
        this.entityData.set(TRAILER_YAW, newYaw);
        this.setYRot(newYaw);
        this.setXRot(0.0f);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Passenger positioning
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!hasPassenger(passenger)) return;

        List<TrailerConfig.SeatConfig> seats = getConfig().seats();
        int seatIndex = this.getPassengers().indexOf(passenger);

        TrailerConfig.SeatConfig seat = (seatIndex >= 0 && seatIndex < seats.size())
                ? seats.get(seatIndex)
                : new TrailerConfig.SeatConfig(0.0, 1.0, 0.0); // fallback: center, 1 block up

        double yawRad = Math.toRadians(this.getYRot());
        double sin    = Math.sin(yawRad);
        double cos    = Math.cos(yawRad);

        double worldX = this.getX() + (seat.offsetX() * cos) + (seat.offsetZ() * sin);
        double worldZ = this.getZ() - (seat.offsetX() * sin) + (seat.offsetZ() * cos);
        double worldY = this.getY() + seat.offsetY();

        moveFunction.accept(passenger, worldX, worldY, worldZ);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Interaction — hitch/unhitch and camo spray
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        // Let spray can be handled by CamoVehicleBase first
        if (player.getItemInHand(hand).is(ModItems.SPRAY.get())) {
            return super.interact(player, hand);
        }

        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // Sneak + right-click = force detach
        if (player.isShiftKeyDown()) {
            if (isAttached()) {
                detach();
                player.displayClientMessage(
                        Component.translatable("fcp.trailer.detached"), true);
            } else {
                player.displayClientMessage(
                        Component.translatable("fcp.trailer.not_attached"), true);
            }
            return InteractionResult.SUCCESS;
        }

        // Already attached → detach
        if (isAttached()) {
            detach();
            player.displayClientMessage(
                    Component.translatable("fcp.trailer.detached"), true);
            return InteractionResult.SUCCESS;
        }

        // Not attached → find and attach to nearest SBW vehicle
        Entity tower = findNearestTower();
        if (tower == null) {
            player.displayClientMessage(
                    Component.translatable("fcp.trailer.no_vehicle_nearby"), true);
            return InteractionResult.SUCCESS;
        }

        attachTo(tower);
        player.displayClientMessage(
                Component.translatable("fcp.trailer.attached"), true);
        return InteractionResult.SUCCESS;
    }

    private static final double HITCH_SEARCH_RADIUS = 6.0;

    @Nullable
    private Entity findNearestTower() {
        List<Entity> candidates = this.level().getEntities(
                this,
                this.getBoundingBox().inflate(HITCH_SEARCH_RADIUS),
                entity -> entity instanceof GeoVehicleEntity
                        && !(entity instanceof AbstractTrailerEntity)
                        && !(entity instanceof Player)
        );

        if (candidates.isEmpty()) return null;

        return candidates.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(this.position())))
                .orElse(null);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Collision suppression
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public boolean canCollideWith(Entity other) {
        if (isAttached()) {
            Optional<UUID> towerUuid = this.entityData.get(TOWER_UUID);
            if (towerUuid.isPresent() && other.getUUID().equals(towerUuid.get())) {
                return false;
            }
        }
        return super.canCollideWith(other);
    }

    @Override
    public boolean isPushable() {
        // While towed the velocity model controls movement — external pushes
        // would fight the constraint correction and cause jitter
        return !isAttached() && super.isPushable();
    }

    @Override
    public void push(Entity other) {
        if (isAttached()) return;
        super.push(other);
    }

    @Override
    public boolean isNoGravity() {
        // While towed with terrain_follow, gravity is handled by move().
        // While towed without terrain_follow, we inherit tower Y velocity instead.
        // Either way, SBW's built-in gravity should not also apply.
        return isAttached() || super.isNoGravity();
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (!isAttached()) {
            super.checkFallDamage(y, onGround, state, pos);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Public hitch API
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Attaches this trailer to the given entity.
     * Snaps position and yaw once so the first applyTowedVelocity() tick
     * starts from a geometrically correct state.
     */
    public void attachTo(Entity tower) {
        this.entityData.set(TOWER_UUID, Optional.of(tower.getUUID()));
        this.entityData.set(IS_TOWED, true);

        TrailerConfig.HitchConfig hitch = getConfig().hitch();
        double trailerLength = getConfig().trailerLength();

        double yawRad = Math.toRadians(tower.getYRot());
        double sin    = Math.sin(yawRad);
        double cos    = Math.cos(yawRad);

        double hitchX = tower.getX() + (hitch.offsetX() * cos) + (hitch.offsetZ() * sin);
        double hitchZ = tower.getZ() - (hitch.offsetX() * sin) + (hitch.offsetZ() * cos);
        double hitchY = tower.getY() + hitch.offsetY();

        double axleX  = hitchX - sin * trailerLength;
        double axleZ  = hitchZ - cos * trailerLength;

        double centerX = (hitchX + axleX) * 0.5;
        double centerZ = (hitchZ + axleZ) * 0.5;

        float snapYaw = tower.getYRot();
        this.entityData.set(TRAILER_YAW, snapYaw);
        prevTrailerYaw = snapYaw;

        // One-time snap on attach only — after this, velocity drives all movement
        this.setPos(centerX, hitchY, centerZ);
        this.setYRot(snapYaw);
        this.setDeltaMovement(Vec3.ZERO);

        cachedTower = tower;
    }

    /**
     * Detaches this trailer. Free SBW vehicle physics resume next tick.
     * The last inherited velocity persists so the trailer rolls naturally.
     */
    public void detach() {
        this.entityData.set(TOWER_UUID, Optional.empty());
        this.entityData.set(IS_TOWED, false);
        cachedTower = null;
    }

    public boolean isAttached() {
        return this.entityData.get(IS_TOWED);
    }

    public float getTrailerYaw() {
        return this.entityData.get(TRAILER_YAW);
    }

    @Nullable
    public Entity getTower() {
        return resolveTower();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Internal tower resolution
    // ════════════════════════════════════════════════════════════════════════

    @Nullable
    private Entity resolveTower() {
        Optional<UUID> uuid = this.entityData.get(TOWER_UUID);
        if (uuid.isEmpty()) return null;

        if (cachedTower != null
                && cachedTower.isAlive()
                && cachedTower.getUUID().equals(uuid.get())) {
            return cachedTower;
        }

        if (this.level() instanceof ServerLevel sl) {
            cachedTower = sl.getEntity(uuid.get());
            if (cachedTower == null) {
                detach();
            }
        }

        return cachedTower;
    }

    // ════════════════════════════════════════════════════════════════════════
    // NBT persistence
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.entityData.get(TOWER_UUID).ifPresent(id -> compound.putUUID("TrailerTowerUUID", id));
        compound.putFloat("TrailerYaw", getTrailerYaw());
        compound.putBoolean("IsTowed", isAttached());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("TrailerTowerUUID")) {
            this.entityData.set(TOWER_UUID, Optional.of(compound.getUUID("TrailerTowerUUID")));
        }
        if (compound.contains("TrailerYaw")) {
            float savedYaw = compound.getFloat("TrailerYaw");
            this.entityData.set(TRAILER_YAW, savedYaw);
            prevTrailerYaw = savedYaw;
        }
        if (compound.contains("IsTowed")) {
            this.entityData.set(IS_TOWED, compound.getBoolean("IsTowed"));
        }
    }
}