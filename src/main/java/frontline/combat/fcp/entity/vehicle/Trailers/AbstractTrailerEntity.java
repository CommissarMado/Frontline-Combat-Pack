package frontline.combat.fcp.entity.vehicle.Trailers;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.tools.OBB;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
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

    /**
     * Extra reach added to the BROAD entity query only (not the accept test). A hitch point
     * sits behind its vehicle's origin and can lie outside that vehicle's own bounding box,
     * so the query has to look further than the configured radius to even see the vehicle.
     */
    private static final double HITCH_QUERY_MARGIN = 10.0;

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

    /**
     * The driver's hitch point in the DRIVER's local space, as synced at attach time.
     * Only meaningful while attached. Synced, so it is valid on the client too.
     */
    public Vec3 getHitchOffset() {
        return new Vec3(this.entityData.get(HITCH_X),
                this.entityData.get(HITCH_Y),
                this.entityData.get(HITCH_Z));
    }

    /**
     * This trailer's tongue point in the TRAILER's local space. Synced, so it is valid
     * on the client too (getTowedData() resolves from datapack configs and is server-side).
     */
    public Vec3 getTowOffset() {
        return new Vec3(this.entityData.get(TOW_X),
                this.entityData.get(TOW_Y),
                this.entityData.get(TOW_Z));
    }

    /** Max articulation angle (degrees) between driver and trailer. Synced. */
    public float getMaxArticulation() {
        return this.entityData.get(MAX_ART);
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

        // The tongue point is only pushed into synced data by attach(). Publish it from
        // the config while detached, so the tongue is known (client included) BEFORE the
        // first hitch — the hitch click zone and the debug overlay both need it. Re-read
        // periodically rather than once, so /reload picks up edits to the JSON live.
        if (!this.level().isClientSide() && !attached && this.tickCount % 20 == 0) {
            syncTowDataFromConfig();
        }

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
        if (isHitchTaken(driver, this)) return false;    // hitch already has a trailer on it

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

    /**
     * This trailer's tongue point in WORLD space — the point that gets pinned to a hitch.
     * Yaw-only rotation about the entity position, the same convention the hitch constraint
     * uses.
     */
    public Vec3 getTongueWorldPos() {
        Vec3 tow = getTowOffset();
        double theta = Math.toRadians(this.getYRot());
        double cos = Math.cos(theta), sin = Math.sin(theta);
        return new Vec3(
                this.getX() + (tow.x * cos - tow.z * sin),
                this.getY() + tow.y,
                this.getZ() + (tow.x * sin + tow.z * cos));
    }

    /** A towing vehicle's hitch point in WORLD space, or null if it can't tow. */
    @Nullable
    public static Vec3 getHitchWorldPos(Entity driver) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(driver.getType());
        if (id == null) return null;
        TrailerDriverData drv = TrailerDriverConfigs.get(id);
        if (drv == null) return null;

        double theta = Math.toRadians(driver.getYRot());
        double cos = Math.cos(theta), sin = Math.sin(theta);
        return new Vec3(
                driver.getX() + (drv.hitchX() * cos - drv.hitchZ() * sin),
                driver.getY() + drv.hitchY(),
                driver.getZ() + (drv.hitchX() * sin + drv.hitchZ() * cos));
    }

    /**
     * Result of a driver search: the connectable vehicle if one was found, plus whether we
     * rejected any candidate purely because its hitch was already in use — so the player
     * can be told "that hitch is taken" instead of a misleading "nothing nearby".
     */
    private record DriverSearch(@Nullable Entity driver, boolean sawTakenHitch) {
    }

    /**
     * True if some OTHER trailer is already hitched to this vehicle. A hitch is a single
     * connection point — without this, two trailers happily pin to the same tractor and
     * fight over the same position.
     */
    private static boolean isHitchTaken(Entity driver, @Nullable AbstractTrailerEntity ignore) {
        List<AbstractTrailerEntity> trailers = driver.level().getEntitiesOfClass(
                AbstractTrailerEntity.class,
                driver.getBoundingBox().inflate(HITCH_QUERY_MARGIN),
                t -> t != ignore && t.isAttached());
        for (AbstractTrailerEntity trailer : trailers) {
            if (trailer.getDriver() == driver) return true;
        }
        return false;
    }

    /**
     * Find the best vehicle to hitch to.
     *
     * A candidate only counts if it offers a hitch we can actually connect to — it has a
     * trailer_driver config (so a real hitch point exists), it's whitelisted for this
     * trailer, its hitch is within reach of our tongue, and that hitch isn't already in use.
     *
     * Distance is measured HITCH-to-TONGUE, not centre-to-centre, so the radius means what
     * you'd expect: how close the two connection points have to be. The nearest hitch wins,
     * which is also the one that will move the least when it snaps.
     */
    private DriverSearch findNearestDriver() {
        TrailerTowedData towed = getTowedData();
        if (towed == null) return new DriverSearch(null, false);

        double radius = towed.attachSearchRadius();
        double radiusSq = radius * radius;
        Vec3 tongue = getTongueWorldPos();

        // The broad query is deliberately wider than the radius: a hitch sits several
        // blocks behind its vehicle's origin and can even fall outside that vehicle's own
        // bounding box, so a tight query would miss vehicles whose hitch is right there.
        // The real tests are the hitch checks below.
        List<Entity> candidates = this.level().getEntities(
                this,
                new AABB(tongue, tongue).inflate(radius + HITCH_QUERY_MARGIN),
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

        if (candidates.isEmpty()) return new DriverSearch(null, false);

        Entity best = null;
        double bestDistSq = Double.MAX_VALUE;
        boolean sawTakenHitch = false;

        for (Entity candidate : candidates) {
            // A real, reachable hitch point — not merely a vehicle that happens to be near.
            Vec3 hitch = getHitchWorldPos(candidate);
            if (hitch == null) continue;                    // no hitch to connect to
            double distSq = hitch.distanceToSqr(tongue);
            if (distSq > radiusSq) continue;                // hitch out of reach of the tongue

            if (isHitchTaken(candidate, this)) {            // hitch exists but is occupied
                sawTakenHitch = true;
                continue;
            }

            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = candidate;
            }
        }
        return new DriverSearch(best, sawTakenHitch);
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

    /** Publish tongue + articulation from the datapack config into synced data. */
    private void syncTowDataFromConfig() {
        TrailerTowedData towed = getTowedData();
        if (towed == null) return; // no config yet (or not loaded) — try again next tick
        this.entityData.set(TOW_X, (float) towed.towX());
        this.entityData.set(TOW_Y, (float) towed.towY());
        this.entityData.set(TOW_Z, (float) towed.towZ());
        this.entityData.set(MAX_ART, towed.maxArticulation());
    }

    /**
     * Fallback radius (blocks) around the tongue, used ONLY when the trailer defines no
     * Interactive OBB. Prefer defining a tongue hitbox in the vehicle JSON instead.
     */
    protected double hitchZoneRadius() {
        return 1.0;
    }

    /** Nudge a surface hit this far toward the box centre before testing containment. */
    private static final double OBB_CONTAINS_EPSILON = 0.01;

    /**
     * True when a click landed on the tongue.
     *
     * Preferred: the trailer declares a tongue hitbox in its vehicle JSON as an OBB with
     * {@code "Part": "Interactive"} — exactly like SBW's MainEngine/Turret part boxes. The
     * click is then tested against that real, rotating box:
     * <pre>
     *   "OBB": [
     *     { "Size": [0.2, 0.2, 0.5], "Position": [0, 0.6, 3.0], "Part": "Interactive" }
     *   ]
     * </pre>
     * Size is HALF-extents, Position is trailer-local, and the box follows the body's
     * rotation. Nothing else in SBW uses the Interactive part on vehicles, so it is free
     * for this and takes no part damage.
     *
     * If no Interactive OBB exists, falls back to a sphere around the tongue point so
     * existing trailers keep working.
     *
     * {@code vec} is the hit position RELATIVE to the entity position on WORLD axes (what
     * interactAt receives). With SBW's OBB picking it is a point on an OBB SURFACE, so it
     * is nudged slightly inward before the containment test — a surface point is otherwise
     * a coin-flip against floating-point error.
     */
    protected boolean isInHitchZone(Vec3 vec) {
        Vec3 world = this.position().add(vec);

        boolean hasZoneBox = false;
        for (OBB obb : this.getOBBs()) {
            if (obb.part != OBB.Part.INTERACTIVE) continue;
            hasZoneBox = true;

            Vec3 centre = new Vec3(obb.center.x, obb.center.y, obb.center.z);
            Vec3 probe = world;
            Vec3 inward = centre.subtract(world);
            if (inward.lengthSqr() > 1.0e-9) {
                probe = world.add(inward.normalize().scale(OBB_CONTAINS_EPSILON));
            }
            if (obb.contains(probe)) return true;
        }
        // A tongue box is defined and the click missed it — no hitch.
        if (hasZoneBox) return false;

        return isNearTonguePoint(vec);
    }

    /** Sphere around the configured tongue point — the no-OBB fallback. */
    private boolean isNearTonguePoint(Vec3 vec) {
        double theta = Math.toRadians(this.getYRot());
        double cos = Math.cos(theta), sin = Math.sin(theta);

        // Inverse of the yaw rotation used everywhere else (x = right, z = forward).
        double lx = vec.x * cos + vec.z * sin;
        double lz = -vec.x * sin + vec.z * cos;
        double ly = vec.y;

        Vec3 tow = getTowOffset();
        double dx = lx - tow.x, dy = ly - tow.y, dz = lz - tow.z;
        double r = hitchZoneRadius();
        return dx * dx + dy * dy + dz * dz <= r * r;
    }

    /**
     * Hitching is offered ONLY for an empty hand clicked near the tongue. Everything else
     * falls through (PASS) to the normal vehicle interaction, so a held item keeps its own
     * behaviour — crowbar pickup, camo spray, and anything SBW does — and subclasses are
     * free to use plain clicks on the body (e.g. opening an inventory).
     */
    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (!player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
        if (!isInHitchZone(vec)) return InteractionResult.PASS;

        if (this.level().isClientSide()) return InteractionResult.SUCCESS;

        if (isAttached()) {
            detach();
            say(player, "fcp.trailer.detached");
            return InteractionResult.SUCCESS;
        }

        DriverSearch search = findNearestDriver();
        Entity driver = search.driver();
        if (driver == null) {
            say(player, search.sawTakenHitch()
                    ? "fcp.trailer.hitch_taken"
                    : "fcp.trailer.no_vehicle_nearby");
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