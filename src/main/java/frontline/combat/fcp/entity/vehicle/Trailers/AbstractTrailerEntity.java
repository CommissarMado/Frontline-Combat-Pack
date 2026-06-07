package frontline.combat.fcp.entity.vehicle.Trailers;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import frontline.combat.fcp.init.FcpTowableConfigs;
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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.registries.BuiltInRegistries;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * AbstractTrailerEntity — base type for all towed trailer entities in FCP.
 *
 * Extends CamoVehicleBase so trailers inherit camo spray, SBW damage,
 * GeckoLib rendering, and all standard vehicle behaviour.
 *
 * ── How it works ─────────────────────────────────────────────────────────────
 *
 * Each tick:
 *   1. Look up the towing vehicle's TowableConfig to get its hitch point
 *      position (defined in the tower's local space via JSON).
 *   2. Rotate the hitch offset by the tower's current yaw to get the
 *      hitch world position.
 *   3. Compute the direction from the trailer's center to that hitch point.
 *   4. Smoothly rotate the trailer to face that direction using rotLerp
 *      with the smoothing value from the TowableConfig.
 *   5. Compute where the trailer center should be (hitch point stepped back
 *      by half the trailer's length along its new facing direction).
 *   6. Express that as deltaMovement and let SBW's move() integrate it —
 *      no setPos(), collisions and terrain work naturally.
 *   7. After super.baseTick(), re-stamp the yaw so SBW can't overwrite it.
 *
 * ── Config files ─────────────────────────────────────────────────────────────
 *
 * Per towing vehicle: data/fcp/towable_vehicles/<entity_id>.json
 *   Defines where the hitch point is on that vehicle.
 *   entity_id must match the vehicle's registry name exactly.
 *
 * Per trailer: data/<namespace>/trailers/<id>.json
 *   Defines the trailer's own geometry (length, hitbox, seats, etc).
 *
 * ── Subclass contract ────────────────────────────────────────────────────────
 *
 *   getConfigId()                → ResourceLocation of the trailer's JSON
 *   getCamoTextures()            → texture array for camo variants
 *   getCamoNames()               → display names for camo variants
 *   registerControllers()        → GeckoLib animation controllers
 *   getAnimatableInstanceCache() → GeckoLib instance cache
 *
 * ── Interaction ──────────────────────────────────────────────────────────────
 *
 *   Right-click              → attach to nearest towable SBW vehicle, or detach
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
     * Current trailer yaw in degrees, always facing the hitch point.
     * Synced so the client can lerp rotation smoothly between packets.
     * Usage in renderer:
     *   Mth.rotLerp(partialTick, entity.prevTrailerYaw, entity.getTrailerYaw())
     */
    private static final EntityDataAccessor<Float> TRAILER_YAW =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.FLOAT);

    // ════════════════════════════════════════════════════════════════════════
    // Client lerp state — read by renderer
    // ════════════════════════════════════════════════════════════════════════

    public float prevTrailerYaw;

    // ════════════════════════════════════════════════════════════════════════
    // Runtime state
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

    protected abstract ResourceLocation getConfigId();

    public final TrailerConfig getConfig() {
        return FcpTrailerConfigs.get(getConfigId());
    }

    // ════════════════════════════════════════════════════════════════════════
    // Synced data setup
    // ════════════════════════════════════════════════════════════════════════

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TOWER_UUID, Optional.empty());
        builder.define(IS_TOWED, false);
        builder.define(TRAILER_YAW, 0.0f);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Rotation — always driven by TRAILER_YAW synced data
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GeckoLib and vanilla both call getViewYRot() to determine which way
     * to render the entity. By returning TRAILER_YAW here (lerped by partial
     * tick) the visual rotation is always correct on both client and server
     * regardless of what SBW's baseTick() does to yRot internally.
     */
    @Override
    public float getViewYRot(float partialTick) {
        if (!isAttached()) return super.getViewYRot(partialTick);
        return Mth.rotLerp(partialTick, prevTrailerYaw, getTrailerYaw());
    }

    /**
     * Called on the client whenever synced entity data changes.
     * Immediately apply TRAILER_YAW to yRot so the entity's own rotation
     * field stays in sync — this is what position packets read on the client.
     */
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key.equals(TRAILER_YAW)) {
            float yaw = getTrailerYaw();
            this.setYRot(yaw);
            this.yRotO = yaw;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Tick
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void baseTick() {
        prevTrailerYaw = getTrailerYaw();

        Entity tower = resolveTower();

        if (tower != null) {
            // Compute and apply velocity BEFORE super.baseTick() so SBW's
            // internal move() call integrates our velocity this tick.
            applyTowedVelocity(tower);
        }

        // Run SBW's full lifecycle — move(), damage, GeckoLib, etc.
        super.baseTick();

        if (tower != null) {
            // Re-stamp yaw AFTER super.baseTick(). SBW's vehicle logic can
            // overwrite yaw during its tick (steering logic, movement alignment).
            // Forcing it here ensures the trailer always faces the hitch point.
            float yaw = getTrailerYaw();
            this.setYRot(yaw);
            this.yRotO = yaw;   // SBW tracks previous yaw here for interpolation
            this.setXRot(0.0f);
            this.xRotO = 0.0f;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Core towing logic
    // ════════════════════════════════════════════════════════════════════════

    private void applyTowedVelocity(Entity tower) {
        // Get the tower's entity registry id and look up its TowableConfig
        ResourceLocation towerId = BuiltInRegistries.ENTITY_TYPE.getKey(tower.getType());
        if (towerId == null) return;

        TowableConfig towable = FcpTowableConfigs.get(towerId);
        if (towable == null) return; // shouldn't happen — we checked on attach

        double trailerLength = getConfig().trailerLength();
        double halfLen = trailerLength * 0.5;

        // ── 1. Compute world-space hitch point from tower ─────────────────
        //
        // The hitch offset is in the tower's local coordinate space.
        // Rotate it by the tower's current yaw to get the world position.
        //
        // Minecraft yaw: 0° = south (+Z), increases clockwise.
        //   worldX = towerX + (hitchX * cos) + (hitchZ * sin)
        //   worldZ = towerZ - (hitchX * sin) + (hitchZ * cos)
        //
        // hitch_z is typically negative (behind the tower center).
        // e.g. hitch_z = -2.5 places the hitch 2.5 blocks behind the tower.
        double towerYawRad = Math.toRadians(tower.getYRot());
        double towerSin    = Math.sin(towerYawRad);
        double towerCos    = Math.cos(towerYawRad);

        double hitchWorldX = tower.getX() + (towable.hitchX() * towerCos) + (towable.hitchZ() * towerSin);
        double hitchWorldZ = tower.getZ() - (towable.hitchX() * towerSin) + (towable.hitchZ() * towerCos);
        double hitchWorldY = tower.getY() + towable.hitchY();

        // ── 2. Compute direction from trailer center to hitch point ────────
        //
        // This vector tells us exactly which way the trailer should face.
        // The trailer's front always points toward the hitch point on the tower.
        double dx = hitchWorldX - this.getX();
        double dz = hitchWorldZ - this.getZ();

        // ── 3. Compute target yaw ─────────────────────────────────────────
        //
        // atan2(-dx, dz) gives Minecraft clockwise yaw from the direction vector.
        // We negate dx because atan2 is counter-clockwise and Minecraft is clockwise.
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

        // ── 4. Smoothly rotate toward target yaw ──────────────────────────
        //
        // Mth.rotLerp correctly handles the 360°/0° wraparound.
        // smoothing comes from the tower's TowableConfig:
        //   1.0 = instant (rigid bar feel)
        //   0.25 = default (natural corner lag)
        //   0.1 = very loose
        float newYaw = Mth.rotLerp(towable.smoothing(), this.getYRot(), targetYaw);

        // ── 5. Compute target center position ─────────────────────────────
        //
        // The trailer's front (hitch connection point) must sit at hitchWorld.
        // Step back halfLen along the trailer's new facing direction to find
        // where the entity center should be.
        double newYawRad = Math.toRadians(newYaw);
        double newSin    = Math.sin(newYawRad);
        double newCos    = Math.cos(newYawRad);

        double targetX = hitchWorldX - newSin * halfLen;
        double targetZ = hitchWorldZ - newCos * halfLen;

        // ── 6. Express as velocity — no setPos() ──────────────────────────
        //
        // Displacement from current center to target center = exact velocity needed.
        // SBW's move() integrates this, so collisions and terrain work normally.
        double velX = targetX - this.getX();
        double velZ = targetZ - this.getZ();

        // Y: preserve downward motion (gravity/ledge drops) when terrain_follow
        // is on so move() handles step-up naturally. When off, match tower Y.
        Vec3 towerVel = tower.getDeltaMovement();
        double velY = getConfig().terrainFollow()
                ? Math.min(this.getDeltaMovement().y, 0.0)
                : towerVel.y;

        // ── 7. Apply velocity and store yaw for post-baseTick re-stamp ────
        this.setDeltaMovement(velX, velY, velZ);
        this.entityData.set(TRAILER_YAW, newYaw);
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
                : new TrailerConfig.SeatConfig(0.0, 1.0, 0.0);

        double yawRad = Math.toRadians(this.getYRot());
        double sin    = Math.sin(yawRad);
        double cos    = Math.cos(yawRad);

        double worldX = this.getX() + (seat.offsetX() * cos) + (seat.offsetZ() * sin);
        double worldZ = this.getZ() - (seat.offsetX() * sin) + (seat.offsetZ() * cos);
        double worldY = this.getY() + seat.offsetY();

        moveFunction.accept(passenger, worldX, worldY, worldZ);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Interaction — attach/detach and camo spray
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        // Spray can handled by CamoVehicleBase
        if (player.getItemInHand(hand).is(ModItems.SPRAY.get())) {
            return super.interact(player, hand);
        }

        if (this.level().isClientSide()) return InteractionResult.SUCCESS;

        // Sneak + right-click = force detach
        if (player.isShiftKeyDown()) {
            if (isAttached()) {
                detach();
                player.displayClientMessage(Component.translatable("fcp.trailer.detached"), true);
            } else {
                player.displayClientMessage(Component.translatable("fcp.trailer.not_attached"), true);
            }
            return InteractionResult.SUCCESS;
        }

        // Already attached → detach
        if (isAttached()) {
            detach();
            player.displayClientMessage(Component.translatable("fcp.trailer.detached"), true);
            return InteractionResult.SUCCESS;
        }

        // Find nearest towable SBW vehicle and attach
        Entity tower = findNearestTower();
        if (tower == null) {
            player.displayClientMessage(Component.translatable("fcp.trailer.no_vehicle_nearby"), true);
            return InteractionResult.SUCCESS;
        }

        attachTo(tower);
        player.displayClientMessage(Component.translatable("fcp.trailer.attached"), true);
        return InteractionResult.SUCCESS;
    }

    private static final double HITCH_SEARCH_RADIUS = 8.0;

    @Nullable
    private Entity findNearestTower() {
        List<Entity> candidates = this.level().getEntities(
                this,
                this.getBoundingBox().inflate(HITCH_SEARCH_RADIUS),
                entity -> {
                    if (!(entity instanceof GeoVehicleEntity)) return false;
                    if (entity instanceof AbstractTrailerEntity) return false;
                    if (entity instanceof Player) return false;
                    // Only allow vehicles that have a TowableConfig registered
                    ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                    return id != null && FcpTowableConfigs.has(id);
                }
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
        return !isAttached() && super.isPushable();
    }

    @Override
    public void push(Entity other) {
        if (isAttached()) return;
        super.push(other);
    }

    @Override
    public boolean isNoGravity() {
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
     * Attaches this trailer to the given tower entity.
     * Performs a one-time position snap so the first tick starts correctly.
     */
    public void attachTo(Entity tower) {
        ResourceLocation towerId = BuiltInRegistries.ENTITY_TYPE.getKey(tower.getType());
        if (towerId == null) return;

        TowableConfig towable = FcpTowableConfigs.get(towerId);
        if (towable == null) return;

        this.entityData.set(TOWER_UUID, Optional.of(tower.getUUID()));
        this.entityData.set(IS_TOWED, true);

        double trailerLength = getConfig().trailerLength();
        double halfLen = trailerLength * 0.5;

        double yawRad  = Math.toRadians(tower.getYRot());
        double sin     = Math.sin(yawRad);
        double cos     = Math.cos(yawRad);

        // Compute hitch world position
        double hitchX = tower.getX() + (towable.hitchX() * cos) + (towable.hitchZ() * sin);
        double hitchZ = tower.getZ() - (towable.hitchX() * sin) + (towable.hitchZ() * cos);
        double hitchY = tower.getY() + towable.hitchY();

        // Trailer center = hitch point stepped back halfLen along tower facing
        double centerX = hitchX - sin * halfLen;
        double centerZ = hitchZ - cos * halfLen;

        float snapYaw = tower.getYRot();
        this.entityData.set(TRAILER_YAW, snapYaw);
        prevTrailerYaw = snapYaw;

        // One-time snap — after this, velocity drives all movement
        this.setPos(centerX, hitchY, centerZ);
        this.setYRot(snapYaw);
        this.yRotO = snapYaw;
        this.setDeltaMovement(Vec3.ZERO);

        cachedTower = tower;
    }

    /**
     * Detaches trailer. Free SBW physics resume next tick.
     * Last velocity is preserved so it rolls naturally rather than stopping dead.
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

        // Re-use cache if still valid this tick
        if (cachedTower != null
                && cachedTower.isAlive()
                && cachedTower.getUUID().equals(uuid.get())) {
            return cachedTower;
        }

        if (this.level() instanceof ServerLevel sl) {
            // Server: direct UUID lookup
            cachedTower = sl.getEntity(uuid.get());
            if (cachedTower == null) {
                detach();
            }
        } else {
            // Client: scan nearby entities for matching UUID.
            // Required so applyTowedVelocity runs client-side and the
            // renderer gets live yaw updates every frame rather than waiting
            // for the next server position packet.
            UUID target = uuid.get();
            cachedTower = null;
            for (Entity e : this.level().getEntitiesOfClass(
                    Entity.class,
                    this.getBoundingBox().inflate(64.0),
                    e -> e.getUUID().equals(target))) {
                cachedTower = e;
                break;
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