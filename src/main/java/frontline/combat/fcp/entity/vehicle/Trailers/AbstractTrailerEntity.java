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
import java.util.UUID;

/**
 * AbstractTrailerEntity — base for every towed trailer in FCP.
 *
 * ── Two-point hitch ──────────────────────────────────────────────────────────
 * A trailer is an SBW vehicle with an "Empty" engine, so SBW applies no driving
 * forces of its own. Each tick the trailer's tongue point is pinned to the
 * driver's hitch point and the body trails and rotates around that pin:
 *
 *   driver hitch point   ← TrailerDriverData  (data/<ns>/trailer_driver/<driver>.json)
 *   trailer tongue point ← TrailerTowedData   (data/<ns>/trailer_towed/<trailer>.json)
 *
 *   1. Hitch world position H = driverPos + R(driverYaw)·driverHitchOffset
 *   2. Trailer yaw φ = heading from the trailer's CURRENT body position to H,
 *      clamped to ±max_articulation of the driver's heading (anti-jackknife).
 *   3. Trailer position so the tongue lands on H:  pos = H − R(φ)·trailerTongueOffset
 * Applied with setPos/setYRot, never velocity (SBW clamps large velocity changes).
 *
 * ── Why the constraint also runs on the client ───────────────────────────────
 * SBW client-PREDICTS the vehicle a player drives (isControlledByLocalInstance),
 * rendering it at its immediate local position. Everything else — including this
 * trailer — is interpolated toward the SERVER position over several ticks. If the
 * trailer only solved on the server, then on the driver's screen the predicted
 * truck would surge ahead while the trailer chased a delayed server stream, and
 * the gap would grow with acceleration.
 *
 * So the constraint runs on BOTH sides. On the client it pins to the client-side
 * (predicted) driver, so the trailer tracks the truck exactly as it is rendered.
 * The hitch/tongue offsets and the driver's network id are synced for this; the
 * config datapacks themselves are only read server-side. While attached, SBW's
 * interpolation (handleClientSync / lerpTo) is suppressed so it can't fight the
 * constraint.
 *
 * ── Local → world transform ──────────────────────────────────────────────────
 * SBW positions with Axis.YP.rotationDegrees(-yaw); the matching local→world for
 * yaw θ (radians), local +Z forward, is:
 *   worldX = x + (lx·cosθ − lz·sinθ);  worldZ = z + (lx·sinθ + lz·cosθ)
 */
public abstract class AbstractTrailerEntity extends CamoVehicleBase {

    // Synced so BOTH sides can solve the constraint against their own driver copy.
    private static final EntityDataAccessor<Integer> DRIVER_ID =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ATTACHED =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> HITCH_X =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HITCH_Y =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HITCH_Z =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TOW_X =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TOW_Y =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TOW_Z =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_ART =
            SynchedEntityData.defineId(AbstractTrailerEntity.class, EntityDataSerializers.FLOAT);

    private static final double ATTACH_SEARCH_RADIUS = 6.0;

    // Anti-glitch guard. The hitch point should never move more than a vehicle can
    // plausibly travel in one tick; a bigger jump is a transient (e.g. client
    // prediction reconciliation throwing the driver's position/velocity for a tick)
    // and snapping to it shows up as a ~90 deg yaw flip that immediately corrects.
    // Jumps beyond this are skipped — invisible because they only last a tick —
    // unless they persist, which means a real teleport that should be honoured.
    private static final double MAX_HITCH_JUMP = 4.0;
    private static final double MAX_HITCH_JUMP_SQ = MAX_HITCH_JUMP * MAX_HITCH_JUMP;
    private static final int MAX_GLITCH_TICKS = 5;

    // A trailer physically cannot reorient this far in a single tick. Real sharp
    // turns stay well under it; a larger demanded swing is the "look-at-hitch" yaw
    // overshooting for one tick as the hitch sweeps sideways during a hard turn.
    // Such ticks keep the previous yaw (so the body never flings out), unless the
    // demand persists, which would be a legitimate fast reorientation.
    private static final float MAX_YAW_STEP = 50.0f;

    /** Consecutive ticks the hitch has looked glitched; transient, not saved. */
    private int hitchGlitchTicks = 0;
    /** Consecutive ticks the demanded yaw swing looked glitched; transient. */
    private int yawGlitchTicks = 0;

    /** Server-side source of truth for the driver, survives save/load. */
    @Nullable
    private UUID driverUUID;

    protected AbstractTrailerEntity(EntityType<? extends GeoVehicleEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DRIVER_ID, -1);
        this.entityData.define(ATTACHED, false);
        this.entityData.define(HITCH_X, 0.0f);
        this.entityData.define(HITCH_Y, 0.5f);
        this.entityData.define(HITCH_Z, 0.0f);
        this.entityData.define(TOW_X, 0.0f);
        this.entityData.define(TOW_Y, 0.5f);
        this.entityData.define(TOW_Z, 0.0f);
        this.entityData.define(MAX_ART, 110.0f);
    }

    /** Tongue / tow rules for THIS trailer, resolved from its registry id (server). */
    @Nullable
    public TrailerTowedData getTowedData() {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(this.getType());
        return id == null ? null : TrailerTowedConfigs.get(id);
    }

    public boolean isAttached() {
        return this.entityData.get(ATTACHED);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void baseTick() {
        boolean attached = isAttached();

        // Anchor render interpolation to last tick's pose before anything moves us,
        // so the partial-tick lerp (xOld→x, yRotO→yRot) stays smooth even though we
        // teleport into place at the end of the tick.
        if (attached) {
            this.xOld = this.getX();
            this.yOld = this.getY();
            this.zOld = this.getZ();
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        super.baseTick();

        if (!attached) return;

        // Suppress SBW's vehicle pitch/bank/roll visuals while towed (see method).
        flattenCosmeticRotation();

        Entity driver = resolveDriver();
        if (driver == null) return; // server may have detached if the driver is gone

        applyHitchConstraint(driver);
    }

    /**
     * A trailer is a towed box, not a self-driven vehicle, so it should never bank,
     * pitch, or roll from SBW's vehicle dynamics. On a setPos-driven follower those
     * fields spike for a single render tick during sharp turns — the model flips
     * ~90 deg "to the side" then corrects. Zeroing the pitch (xRot) and roll
     * (roll / prevRoll) each tick leaves the renderer with only the yaw we drive.
     * Only fields present in the SBW jar being built against are set here.
     */
    private void flattenCosmeticRotation() {
        this.setXRot(0.0f);
        this.xRotO = 0.0f;
        this.setZRot(0.0f);       // roll
        this.setPrevRoll(0.0f);
    }

    private void applyHitchConstraint(Entity driver) {
        // Tick-order compensation: if the driver hasn't run its own tick yet this
        // game tick its reported position is last tick's, which would leave the
        // trailer one tick behind. Anticipate its horizontal movement to cancel that.
        double antX = 0.0, antZ = 0.0;
        if (driver.tickCount < this.tickCount) {
            Vec3 dv = driver.getDeltaMovement();
            antX = dv.x;
            antZ = dv.z;
        }
        double driverX = driver.getX() + antX;
        double driverZ = driver.getZ() + antZ;
        double driverY = driver.getY();

        double hitchX = this.entityData.get(HITCH_X);
        double hitchY = this.entityData.get(HITCH_Y);
        double hitchZ = this.entityData.get(HITCH_Z);
        double towX = this.entityData.get(TOW_X);
        double towY = this.entityData.get(TOW_Y);
        double towZ = this.entityData.get(TOW_Z);
        float maxArt = this.entityData.get(MAX_ART);

        // 1. Hitch world position from the driver.
        double thetaD = Math.toRadians(driver.getYRot());
        double cosD = Math.cos(thetaD), sinD = Math.sin(thetaD);
        double hx = driverX + (hitchX * cosD - hitchZ * sinD);
        double hz = driverZ + (hitchX * sinD + hitchZ * cosD);
        double hy = driverY + hitchY;

        // Reject implausible single-tick hitch jumps (transient prediction spikes).
        // Compare the new hitch to where the tongue currently sits; a real vehicle
        // can't move it more than MAX_HITCH_JUMP in one tick. Skip the glitch tick
        // entirely, but give up and accept it if it persists (a genuine teleport).
        double thetaCur = Math.toRadians(this.getYRot());
        double cosC = Math.cos(thetaCur), sinC = Math.sin(thetaCur);
        double tongueX = this.getX() + (towX * cosC - towZ * sinC);
        double tongueZ = this.getZ() + (towX * sinC + towZ * cosC);
        double jx = hx - tongueX, jz = hz - tongueZ;
        if (jx * jx + jz * jz > MAX_HITCH_JUMP_SQ && hitchGlitchTicks < MAX_GLITCH_TICKS) {
            hitchGlitchTicks++;
            this.setDeltaMovement(Vec3.ZERO);
            return; // hold position this tick; the hitch is sane again next tick
        }
        hitchGlitchTicks = 0;

        // 2. Trailing yaw: aim the trailer's front from its current body to the hitch.
        double dx = hx - this.getX();
        double dz = hz - this.getZ();
        float yaw;
        if (dx * dx + dz * dz < 1.0e-6) {
            yaw = this.getYRot();
        } else {
            yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        }
        float rel = Mth.wrapDegrees(yaw - driver.getYRot());
        if (rel > maxArt) rel = maxArt;
        if (rel < -maxArt) rel = -maxArt;
        yaw = Mth.wrapDegrees(driver.getYRot() + rel);

        // Reject a physically-impossible single-tick yaw swing. During a hard turn
        // the instantaneous look-at-hitch heading can spike for one tick and fling
        // the whole body sideways (since position is derived from yaw below). Keep
        // the previous heading on such a tick — invisible because it self-corrects
        // next tick — but accept it if it persists (a real fast reorientation).
        float yawStep = Mth.wrapDegrees(yaw - this.getYRot());
        if (Math.abs(yawStep) > MAX_YAW_STEP && yawGlitchTicks < MAX_GLITCH_TICKS) {
            yawGlitchTicks++;
            yaw = this.getYRot();
        } else {
            yawGlitchTicks = 0;
        }

        // 3. Position so the tongue lands on the hitch.
        double thetaT = Math.toRadians(yaw);
        double cosT = Math.cos(thetaT), sinT = Math.sin(thetaT);
        double newX = hx - (towX * cosT - towZ * sinT);
        double newZ = hz - (towX * sinT + towZ * cosT);
        double newY = hy - towY;

        this.setPos(newX, newY, newZ);
        this.setYRot(yaw);
        this.setXRot(0.0f);
        this.setDeltaMovement(Vec3.ZERO);
    }

    // ── Attach / detach ─────────────────────────────────────────────────────────

    public boolean attach(Entity driver) {
        if (driver == null) return false;
        if (this.level().isClientSide()) return false;

        ResourceLocation driverId = ForgeRegistries.ENTITY_TYPES.getKey(driver.getType());
        if (driverId == null) return false;

        TrailerDriverData drv = TrailerDriverConfigs.get(driverId);
        if (drv == null) return false;                  // driver can't tow

        TrailerTowedData towed = getTowedData();
        if (towed == null) return false;                // trailer has no tow config
        if (!towed.canBeTowedBy(driverId)) return false; // not whitelisted

        this.driverUUID = driver.getUUID();
        this.entityData.set(DRIVER_ID, driver.getId());
        this.entityData.set(ATTACHED, true);
        this.entityData.set(HITCH_X, (float) drv.hitchX());
        this.entityData.set(HITCH_Y, (float) drv.hitchY());
        this.entityData.set(HITCH_Z, (float) drv.hitchZ());
        this.entityData.set(TOW_X, (float) towed.towX());
        this.entityData.set(TOW_Y, (float) towed.towY());
        this.entityData.set(TOW_Z, (float) towed.towZ());
        this.entityData.set(MAX_ART, towed.maxArticulation());

        // One-time snap: face the driver's heading, tongue on the hitch.
        double thetaD = Math.toRadians(driver.getYRot());
        double cosD = Math.cos(thetaD), sinD = Math.sin(thetaD);
        double hx = driver.getX() + (drv.hitchX() * cosD - drv.hitchZ() * sinD);
        double hz = driver.getZ() + (drv.hitchX() * sinD + drv.hitchZ() * cosD);
        double hy = driver.getY() + drv.hitchY();
        float yaw = driver.getYRot();
        double newX = hx - (towed.towX() * cosD - towed.towZ() * sinD);
        double newZ = hz - (towed.towX() * sinD + towed.towZ() * cosD);
        double newY = hy - towed.towY();

        this.setPos(newX, newY, newZ);
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.setXRot(0.0f);
        this.setDeltaMovement(Vec3.ZERO);
        return true;
    }

    public void detach() {
        this.driverUUID = null;
        this.entityData.set(DRIVER_ID, -1);
        this.entityData.set(ATTACHED, false);
    }

    @Nullable
    public Entity getDriver() {
        return resolveDriver();
    }

    @Nullable
    private Entity resolveDriver() {
        if (!isAttached()) return null;

        if (this.level() instanceof ServerLevel sl) {
            // Server: resolve by UUID (robust across reload), keep the synced id fresh.
            Entity e = driverUUID == null ? null : sl.getEntity(driverUUID);
            if (e == null) {
                detach();
                return null;
            }
            if (this.entityData.get(DRIVER_ID) != e.getId()) {
                this.entityData.set(DRIVER_ID, e.getId());
            }
            return e;
        }

        // Client: resolve purely by the synced network id.
        int id = this.entityData.get(DRIVER_ID);
        if (id < 0) return null;
        return this.level().getEntity(id);
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

    // ── Suppress SBW interpolation while attached (the constraint owns position) ─

    @Override
    public void handleClientSync() {
        if (isAttached()) return;
        super.handleClientSync();
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        if (isAttached()) return; // ignore the server position stream; we follow the driver locally
        super.lerpTo(x, y, z, yaw, pitch, steps, teleport);
    }

    // ── Interaction (single source of truth for attach/detach) ──────────────────

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).is(ModItems.SPRAY.get())) {
            return super.interact(player, hand); // camo
        }

        if (this.level().isClientSide()) return InteractionResult.SUCCESS;

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

    // ── Behaviour while hitched ─────────────────────────────────────────────────

    @Override
    public boolean canCollideWith(Entity other) {
        if (isAttached() && other.getId() == this.entityData.get(DRIVER_ID)) return false;
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

    // ── Persistence ─────────────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("TrailerAttached", isAttached());
        if (driverUUID != null) compound.putUUID("TrailerDriverUUID", driverUUID);
        compound.putFloat("HitchX", this.entityData.get(HITCH_X));
        compound.putFloat("HitchY", this.entityData.get(HITCH_Y));
        compound.putFloat("HitchZ", this.entityData.get(HITCH_Z));
        compound.putFloat("TowX", this.entityData.get(TOW_X));
        compound.putFloat("TowY", this.entityData.get(TOW_Y));
        compound.putFloat("TowZ", this.entityData.get(TOW_Z));
        compound.putFloat("MaxArt", this.entityData.get(MAX_ART));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(ATTACHED, compound.getBoolean("TrailerAttached"));
        this.driverUUID = compound.hasUUID("TrailerDriverUUID") ? compound.getUUID("TrailerDriverUUID") : null;
        if (compound.contains("HitchX")) {
            this.entityData.set(HITCH_X, compound.getFloat("HitchX"));
            this.entityData.set(HITCH_Y, compound.getFloat("HitchY"));
            this.entityData.set(HITCH_Z, compound.getFloat("HitchZ"));
            this.entityData.set(TOW_X, compound.getFloat("TowX"));
            this.entityData.set(TOW_Y, compound.getFloat("TowY"));
            this.entityData.set(TOW_Z, compound.getFloat("TowZ"));
            this.entityData.set(MAX_ART, compound.getFloat("MaxArt"));
        }
    }
}