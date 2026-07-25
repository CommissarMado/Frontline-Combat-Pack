package frontline.combat.fcp.entity.vehicle.Trailers;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
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
 * Base for towed trailers. An SBW vehicle with an "Empty" engine; each tick the tongue
 * point is pinned to the driver's hitch point:
 *   1. hitch H = driverPos + R(driverYaw)·hitchOffset      (from trailer_driver JSON)
 *   2. yaw = heading from trailer body to H, clamped to ±max_articulation
 *   3. pos = H − R(yaw)·tongueOffset                        (from trailer_towed JSON)
 * Applied with setPos/setYRot, never velocity (SBW clamps large velocity changes).
 *
 * The constraint runs on BOTH sides: SBW client-predicts the driven vehicle, so a
 * server-only trailer would visibly lag the truck. Offsets and the driver's network id
 * are synced for this; datapack configs are only read server-side. While attached,
 * SBW's interpolation is suppressed so it can't fight the constraint.
 */
public abstract class AbstractTrailerEntity extends CamoVehicleBase {

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

    /** Extra reach for broad entity queries only — a hitch can sit outside its vehicle's box. */
    private static final double HITCH_QUERY_MARGIN = 10.0;

    // Glitch guards: reject single-tick hitch jumps / yaw swings no real vehicle could
    // produce (client prediction spikes), unless they persist — then it's a real
    // teleport / fast reorientation and gets honoured.
    private static final double MAX_HITCH_JUMP_SQ = 4.0 * 4.0;
    private static final float MAX_YAW_STEP = 50.0f;
    private static final int MAX_GLITCH_TICKS = 5;

    /** Server ticks to wait for a missing driver (chunk load order, dimension sync) before detaching. */
    private static final int MISSING_DRIVER_GRACE_TICKS = 600;

    // Terrain pitch: sampled fore/aft of the axle each tick, smoothed so single-block
    // steps read as a bump rather than a snap.
    private static final float MAX_TERRAIN_PITCH = 35.0f;
    private static final float PITCH_SMOOTHING = 0.25f;
    private static final int GROUND_PROBE_DEPTH = 8;

    /** Ticks over which a fresh hitch eases into place instead of teleporting. */
    private static final int ATTACH_LERP_TICKS = 10;

    private int hitchGlitchTicks = 0;
    private int yawGlitchTicks = 0;
    private int missingDriverTicks = 0;
    private float terrainPitch = 0.0f;
    private int attachLerpTicks = 0;
    private boolean wasAttached = false;

    /** Server-side source of truth for the driver; survives save/load. */
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

    /** Rotate a local-space offset by yaw (degrees) and add the given origin. */
    private static Vec3 localToWorld(double ox, double oy, double oz, float yawDeg,
                                     double lx, double ly, double lz) {
        return localToWorldTilted(ox, oy, oz, yawDeg, 0.0f, lx, ly, lz);
    }

    /**
     * As localToWorld, but the offset is first pitched about X (vanilla xRot sign:
     * positive = nose down). This is what puts a rear hitch HIGHER when the truck noses
     * down a slope — yaw-only maths kept the hitch at flat-ground height, which is the
     * desync this fixes. Roll is ignored: hitch/tongue offsets sit on the centreline
     * (x ~ 0), so its contribution is negligible.
     */
    private static Vec3 localToWorldTilted(double ox, double oy, double oz, float yawDeg,
                                           float pitchDeg, double lx, double ly, double lz) {
        double pitch = Math.toRadians(pitchDeg);
        double cp = Math.cos(pitch), sp = Math.sin(pitch);
        double py = ly * cp - lz * sp;
        double pz = ly * sp + lz * cp;

        double theta = Math.toRadians(yawDeg);
        double cos = Math.cos(theta), sin = Math.sin(theta);
        return new Vec3(ox + (lx * cos - pz * sin), oy + py, oz + (lx * sin + pz * cos));
    }

    @Nullable
    public TrailerTowedData getTowedData() {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(this.getType());
        return id == null ? null : TrailerTowedConfigs.get(id);
    }

    public boolean isAttached() {
        return this.entityData.get(ATTACHED);
    }

    /** True if this trailer is hitched to the given entity. Valid on both sides, no side effects. */
    public boolean isDrivenBy(Entity candidate) {
        if (!isAttached()) return false;
        if (driverUUID != null) return candidate.getUUID().equals(driverUUID);
        return candidate.getId() == this.entityData.get(DRIVER_ID);
    }

    public Vec3 getHitchOffset() {
        return new Vec3(this.entityData.get(HITCH_X),
                this.entityData.get(HITCH_Y),
                this.entityData.get(HITCH_Z));
    }

    public Vec3 getTowOffset() {
        return new Vec3(this.entityData.get(TOW_X),
                this.entityData.get(TOW_Y),
                this.entityData.get(TOW_Z));
    }

    public float getMaxArticulation() {
        return this.entityData.get(MAX_ART);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void baseTick() {
        boolean attached = isAttached();

        // Anchor render interpolation to last tick's pose; the constraint teleports us at
        // the end of the tick and the partial-tick lerp must stay smooth.
        if (attached) {
            this.xOld = this.getX();
            this.yOld = this.getY();
            this.zOld = this.getZ();
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        super.baseTick();

        // Publish the tongue point while detached so the hitch click zone and debug overlay
        // know it before the first attach; periodic so /reload edits take effect live.
        if (!this.level().isClientSide() && !attached && this.tickCount % 20 == 0) {
            syncTowDataFromConfig();
        }

        if (attached && !this.wasAttached) {
            this.attachLerpTicks = ATTACH_LERP_TICKS; // ease in on whichever side just learned
        }
        this.wasAttached = attached;
        if (!attached) {
            this.attachLerpTicks = 0;
            return;
        }

        flattenCosmeticRotation();

        Entity driver = resolveDriver();
        if (driver == null) return;

        applyHitchConstraint(driver);
    }

    /**
     * Zero SBW's bank/roll each tick — on a setPos-driven follower they spike for a render
     * tick during sharp turns, flipping the model sideways. Pitch is NOT zeroed: the
     * constraint sets it from the terrain every tick, so SBW never gets a word in anyway.
     */
    private void flattenCosmeticRotation() {
        this.setZRot(0.0f);
        this.setPrevRoll(0.0f);
    }

    private void applyHitchConstraint(Entity driver) {
        // If the driver hasn't ticked yet this game tick, its position is one tick old;
        // anticipate its horizontal movement so the trailer doesn't trail a tick behind.
        double antX = 0.0, antZ = 0.0;
        if (driver.tickCount < this.tickCount) {
            Vec3 dv = driver.getDeltaMovement();
            antX = dv.x;
            antZ = dv.z;
        }

        Vec3 hitchOff = getHitchOffset();
        Vec3 towOff = getTowOffset();
        float maxArt = this.entityData.get(MAX_ART);

        // Where the hitch is DRAWN. SBW rotates a vehicle about a raised pivot
        // (0, rotateOffsetHeight, 0), by roll+fakeRoll then xRot+fakePitch then yaw — not
        // about the entity origin. Rotating the offset about the origin was the desync
        // that appeared exactly (and only) when the towing vehicle tilted: the pivot term
        // vanishes untilted, and yaw alone is pivot-agnostic since the pivot sits on the
        // yaw axis.
        Vec3 hitch = driverHitchWorld(driver, driver.getX() + antX, driver.getY(), driver.getZ() + antZ,
                driver.getYRot(), hitchOff);

        boolean easing = this.attachLerpTicks > 0;

        // Reject implausible single-tick hitch jumps (see glitch guards above). Not while
        // easing in: a fresh hitch legitimately starts several blocks away — the guard
        // would freeze exactly the gap the ease exists to close.
        if (!easing) {
            Vec3 tongue = getTongueWorldPos();
            double jx = hitch.x - tongue.x, jz = hitch.z - tongue.z;
            if (jx * jx + jz * jz > MAX_HITCH_JUMP_SQ && hitchGlitchTicks < MAX_GLITCH_TICKS) {
                hitchGlitchTicks++;
                this.setDeltaMovement(Vec3.ZERO);
                return;
            }
            hitchGlitchTicks = 0;
        }

        // Trailing yaw: aim at the hitch, clamped to ±maxArt of the driver's heading.
        double dx = hitch.x - this.getX();
        double dz = hitch.z - this.getZ();
        float yaw = dx * dx + dz * dz < 1.0e-6
                ? this.getYRot()
                : (float) Math.toDegrees(Math.atan2(-dx, dz));
        float rel = Mth.clamp(Mth.wrapDegrees(yaw - driver.getYRot()), -maxArt, maxArt);
        yaw = Mth.wrapDegrees(driver.getYRot() + rel);

        // Reject an impossible single-tick yaw swing (position derives from yaw below,
        // so a spike would fling the whole body sideways). Also skipped while easing —
        // swinging round to the hitch heading IS the point then.
        if (!easing) {
            float yawStep = Mth.wrapDegrees(yaw - this.getYRot());
            if (Math.abs(yawStep) > MAX_YAW_STEP && yawGlitchTicks < MAX_GLITCH_TICKS) {
                yawGlitchTicks++;
                yaw = this.getYRot();
            } else {
                yawGlitchTicks = 0;
            }
        }

        updateTerrainPitch(yaw, hitch);

        if (easing) {
            // Close 1/n of the remaining gap with n counting down — lands EXACTLY on the
            // hitch on the final tick, and every step is an ordinary smooth move.
            float alpha = 1.0f / this.attachLerpTicks;
            this.attachLerpTicks--;

            Vec3 tongueFromOrigin = localToWorldTilted(0, 0, 0, yaw, this.terrainPitch,
                    towOff.x, towOff.y, towOff.z);
            double tx = hitch.x - tongueFromOrigin.x;
            double ty = hitch.y - tongueFromOrigin.y;
            double tz = hitch.z - tongueFromOrigin.z;

            this.setPos(Mth.lerp(alpha, this.getX(), tx),
                    Mth.lerp(alpha, this.getY(), ty),
                    Mth.lerp(alpha, this.getZ(), tz));
            this.setYRot(Mth.rotLerp(alpha, this.getYRot(), yaw));
            this.setXRot(Mth.rotLerp(alpha, this.getXRot(), this.terrainPitch));
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        placeTongueOnHitch(hitch, yaw, this.terrainPitch, towOff);
    }

    /**
     * The pitch a vehicle is DRAWN at: xRot plus SBW's inertia sway (fakePitch).
     *
     * fakePitch is read by REFLECTION, resolved once and cached: its Java-visible shape
     * differs across SBW builds (a public field in the Java-era jars, a getter after the
     * Kotlin conversion), so a direct reference compiles against one and breaks on the
     * other. This works against either, and simply contributes 0 on a build without it.
     */
    /**
     * A hitch offset carried through the DRIVER's full rendered orientation, exactly as
     * SBW 0.8.9 draws it: rotate (offset − pivot) by roll, then pitch, then yaw, about the
     * pivot (0, rotateOffsetHeight, 0), then translate back. Pitch is xRot + fakePitch,
     * roll is roll + fakeRoll.
     */
    private static Vec3 driverHitchWorld(Entity driver, double ox, double oy, double oz,
                                         float yawDeg, Vec3 offset) {
        double pivotY = sbwFloat(driver, ROTATE_OFFSET_HEIGHT);
        float pitchDeg = driver.getXRot() + sbwFloat(driver, FAKE_PITCH);
        float rollDeg = sbwFloat(driver, ROLL) + sbwFloat(driver, FAKE_ROLL);

        double lx = offset.x, ly = offset.y - pivotY, lz = offset.z;

        double r = Math.toRadians(rollDeg);
        double cr = Math.cos(r), sr = Math.sin(r);
        double rx = lx * cr - ly * sr;
        double ry = lx * sr + ly * cr;

        Vec3 world = localToWorldTilted(ox, oy + pivotY, oz, yawDeg, pitchDeg, rx, ry, lz);
        return world;
    }

    /** As driverHitchWorld, with the vanilla-synced pitch component frame-interpolated. */
    private static Vec3 driverHitchWorldLerped(Entity driver, Vec3 pos, float yawDeg,
                                               float partialTick, Vec3 offset) {
        double pivotY = sbwFloat(driver, ROTATE_OFFSET_HEIGHT);
        float pitchDeg = Mth.lerp(partialTick, driver.xRotO, driver.getXRot())
                + sbwFloat(driver, FAKE_PITCH);
        float rollDeg = sbwFloat(driver, ROLL) + sbwFloat(driver, FAKE_ROLL);

        double lx = offset.x, ly = offset.y - pivotY, lz = offset.z;
        double r = Math.toRadians(rollDeg);
        double cr = Math.cos(r), sr = Math.sin(r);
        double rx = lx * cr - ly * sr;
        double ry = lx * sr + ly * cr;

        return localToWorldTilted(pos.x, pos.y + pivotY, pos.z, yawDeg, pitchDeg, rx, ry, lz);
    }

    /** Read a float-valued SBW property through a cached handle; 0 when absent. */
    private static float sbwFloat(Entity driver, @Nullable java.lang.invoke.MethodHandle handle) {
        if (handle != null && driver instanceof VehicleEntity vehicle) {
            try {
                return ((Number) handle.invoke(vehicle)).floatValue();
            } catch (Throwable ignored) {
            }
        }
        return 0.0f;
    }

    // SBW symbols read by REFLECTION, resolved once: their Java-visible shape differs
    // across SBW builds (fields in Java-era jars, getters after the Kotlin conversion),
    // so a direct reference compiles against one and breaks on the other. Each degrades
    // to 0 when a build lacks it.
    @Nullable
    private static final java.lang.invoke.MethodHandle FAKE_PITCH = resolveSbw("getFakePitch", "fakePitch");
    @Nullable
    private static final java.lang.invoke.MethodHandle FAKE_ROLL = resolveSbw("getFakeRoll", "fakeRoll");
    @Nullable
    private static final java.lang.invoke.MethodHandle ROLL = resolveSbw("getRoll", "roll");
    @Nullable
    private static final java.lang.invoke.MethodHandle ROTATE_OFFSET_HEIGHT =
            resolveSbw("getRotateOffsetHeight", "rotateOffsetHeight");

    @Nullable
    private static java.lang.invoke.MethodHandle resolveSbw(String getter, String field) {
        java.lang.invoke.MethodHandles.Lookup lookup = java.lang.invoke.MethodHandles.lookup();
        try {
            return lookup.unreflect(VehicleEntity.class.getMethod(getter));
        } catch (Exception ignored) {
        }
        try {
            return lookup.unreflectGetter(VehicleEntity.class.getField(field));
        } catch (Exception ignored) {
        }
        return null;
    }

    /** Position the body so the tongue lands on the hitch at the given yaw and pitch. */
    private void placeTongueOnHitch(Vec3 hitch, float yaw, float pitch, Vec3 towOff) {
        Vec3 tongueFromOrigin = localToWorldTilted(0, 0, 0, yaw, pitch, towOff.x, towOff.y, towOff.z);
        this.setPos(hitch.x - tongueFromOrigin.x, hitch.y - tongueFromOrigin.y, hitch.z - tongueFromOrigin.z);
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.setDeltaMovement(Vec3.ZERO);
    }

    /**
     * Pitch for a body that HANGS from the hitch at the front and rests on the ground at
     * the rear: the angle of the hitch-to-rear-ground line.
     *
     * Terrain under the front half is deliberately not consulted — the front doesn't touch
     * it. That was the long-tongue desync: with the truck up a slope and the trailer on
     * flat ground, terrain-under-body said "level", so the trailer refused to tilt and the
     * tongue solve floated the whole body to hitch height instead.
     *
     * tow.y is subtracted from the hitch height so a flat run reads as 0 (coupler and
     * axle sit at comparable heights). Smoothed so a one-block step reads as a bump,
     * clamped so a cliff edge can't fold the trailer.
     */
    private void updateTerrainPitch(float yaw, Vec3 hitch) {
        Vec3 tow = getTowOffset();
        double rearZ = -Math.max(1.5, Math.abs(tow.z)); // rear contact, mirroring the tongue
        double run = Math.abs(tow.z - rearZ);

        Vec3 rear = localToWorld(this.getX(), this.getY(), this.getZ(), yaw, 0, 0, rearZ);
        double rearGround = groundYAt(rear);

        double dy = (hitch.y - tow.y) - rearGround;
        float target = (float) -Math.toDegrees(Math.atan2(dy, run));
        target = Mth.clamp(target, -MAX_TERRAIN_PITCH, MAX_TERRAIN_PITCH);
        this.terrainPitch = this.terrainPitch + (target - this.terrainPitch) * PITCH_SMOOTHING;
    }

    /** Top of the first block with collision at/below the given point; current Y if none found. */
    private double groundYAt(Vec3 point) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
                Mth.floor(point.x), Mth.floor(point.y + 1.0), Mth.floor(point.z));
        if (!this.level().hasChunkAt(pos)) return point.y;

        for (int i = 0; i <= GROUND_PROBE_DEPTH; i++) {
            BlockState state = this.level().getBlockState(pos);
            if (!state.isAir()) {
                var shape = state.getCollisionShape(this.level(), pos);
                if (!shape.isEmpty()) {
                    return pos.getY() + shape.max(net.minecraft.core.Direction.Axis.Y);
                }
            }
            pos.move(0, -1, 0);
        }
        return point.y;
    }

    // ── Attach / detach ─────────────────────────────────────────────────────────

    public boolean attach(Entity driver) {
        if (driver == null) return false;
        if (this.level().isClientSide()) return false;

        ResourceLocation driverId = ForgeRegistries.ENTITY_TYPES.getKey(driver.getType());
        if (driverId == null) return false;

        TrailerDriverData drv = TrailerDriverConfigs.get(driverId);
        if (drv == null) return false;
        TrailerTowedData towed = getTowedData();
        if (towed == null) return false;
        if (!towed.canBeTowedBy(driverId)) return false;
        if (isHitchTaken(driver, this)) return false;

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
        this.hitchGlitchTicks = 0;
        this.yawGlitchTicks = 0;
        this.missingDriverTicks = 0;

        // No snap here: the attach edge in baseTick starts an ease on BOTH sides (this
        // method is server-only), so the trailer swings smoothly onto the hitch instead of
        // teleporting.
        return true;
    }

    public void detach() {
        this.driverUUID = null;
        this.entityData.set(DRIVER_ID, -1);
        this.entityData.set(ATTACHED, false);
        this.hitchGlitchTicks = 0;
        this.yawGlitchTicks = 0;
        this.missingDriverTicks = 0;
        this.terrainPitch = 0.0f;
        this.attachLerpTicks = 0;
    }

    @Nullable
    public Entity getDriver() {
        return resolveDriver();
    }

    @Nullable
    private Entity resolveDriver() {
        if (!isAttached()) return null;

        if (this.level() instanceof ServerLevel sl) {
            Entity e = driverUUID == null ? null : sl.getEntity(driverUUID);
            if (e == null) {
                // The driver's chunk may simply not be loaded yet (world load order,
                // dimension sync). Give it time before concluding it's really gone.
                if (driverUUID == null || ++missingDriverTicks > MISSING_DRIVER_GRACE_TICKS) {
                    detach();
                }
                return null;
            }
            missingDriverTicks = 0;
            if (this.entityData.get(DRIVER_ID) != e.getId()) {
                this.entityData.set(DRIVER_ID, e.getId()); // network id changes across reloads
            }
            return e;
        }

        int id = this.entityData.get(DRIVER_ID);
        return id < 0 ? null : this.level().getEntity(id);
    }

    /**
     * World-space correction to render the trailer with its tongue EXACTLY on the driver's
     * rendered hitch this frame.
     *
     * The tick solve is exact at tick boundaries, but between them the trailer's pose is
     * interpolated LINEARLY while the tongue is a rotating offset — so mid-frame the
     * tongue drifts off the hitch by an error that grows with the offset lengths and the
     * turn rate. This recomputes both attachment points from the interpolated render
     * poses (driver sway included) and returns the difference for the renderer to
     * translate by. Zero while detached or still easing on.
     */
    public Vec3 renderPinCorrection(float partialTick) {
        if (!isAttached() || this.attachLerpTicks > 0) return Vec3.ZERO;
        Entity driver = resolveDriver();
        if (driver == null) return Vec3.ZERO;

        Vec3 hitchOff = getHitchOffset();
        float driverYaw = Mth.rotLerp(partialTick, driver.yRotO, driver.getYRot());
        Vec3 driverPos = driver.getPosition(partialTick);
        // Full rendered orientation (pivot, roll, sway) — same transform the tick solve
        // uses. xRot is lerped for the frame; the sway/roll terms decay smoothly enough
        // that their current-tick values are visually exact.
        Vec3 hitch = driverHitchWorldLerped(driver, driverPos, driverYaw, partialTick, hitchOff);

        Vec3 tow = getTowOffset();
        float myYaw = Mth.rotLerp(partialTick, this.yRotO, this.getYRot());
        float myPitch = Mth.lerp(partialTick, this.xRotO, this.getXRot());
        Vec3 myPos = this.getPosition(partialTick);
        Vec3 tongue = localToWorldTilted(myPos.x, myPos.y, myPos.z,
                myYaw, myPitch, tow.x, tow.y, tow.z);

        Vec3 fix = hitch.subtract(tongue);
        // A large correction means something upstream is wrong (teleport, glitch tick) —
        // don't smear the model across the world for it.
        return fix.lengthSqr() > 4.0 ? Vec3.ZERO : fix;
    }

    /**
     * Rotate a body-local offset by the trailer's CURRENT yaw and pitch (no origin added).
     * For anything that follows the body — implement rows, attachment points — so a pitched
     * trailer's working parts are where the model shows them, not at flat-ground positions.
     */
    protected Vec3 rotateBodyLocal(double lx, double ly, double lz) {
        return localToWorldTilted(0, 0, 0, this.getYRot(), this.getXRot(), lx, ly, lz);
    }

    /** This trailer's tongue point in world space — the point that gets pinned to a hitch. */
    public Vec3 getTongueWorldPos() {
        Vec3 tow = getTowOffset();
        return localToWorldTilted(this.getX(), this.getY(), this.getZ(),
                this.getYRot(), this.getXRot(), tow.x, tow.y, tow.z);
    }

    /** A towing vehicle's hitch point in world space, or null if it can't tow. */
    @Nullable
    public static Vec3 getHitchWorldPos(Entity driver) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(driver.getType());
        if (id == null) return null;
        TrailerDriverData drv = TrailerDriverConfigs.get(id);
        if (drv == null) return null;
        return localToWorld(driver.getX(), driver.getY(), driver.getZ(),
                driver.getYRot(), drv.hitchX(), drv.hitchY(), drv.hitchZ());
    }

    private record DriverSearch(@Nullable Entity driver, boolean sawTakenHitch) {
    }

    /** True if some OTHER trailer is already hitched to this vehicle — a hitch is one point. */
    private static boolean isHitchTaken(Entity driver, @Nullable AbstractTrailerEntity ignore) {
        return !driver.level().getEntitiesOfClass(
                AbstractTrailerEntity.class,
                driver.getBoundingBox().inflate(HITCH_QUERY_MARGIN),
                t -> t != ignore && t.isDrivenBy(driver)).isEmpty();
    }

    /**
     * Find the nearest connectable hitch. Distance is measured HITCH-to-TONGUE, not
     * centre-to-centre, so attach_search_radius means how close the two connection points
     * must be. Tracks whether any candidate was rejected only because its hitch was in use,
     * so the player gets "hitch taken" rather than a misleading "nothing nearby".
     */
    private DriverSearch findNearestDriver() {
        TrailerTowedData towed = getTowedData();
        if (towed == null) return new DriverSearch(null, false);

        double radiusSq = towed.attachSearchRadius() * towed.attachSearchRadius();
        Vec3 tongue = getTongueWorldPos();

        // Broad query is wider than the radius on purpose: a hitch sits behind its
        // vehicle's origin and can fall outside that vehicle's own bounding box.
        List<Entity> candidates = this.level().getEntities(
                this,
                new AABB(tongue, tongue).inflate(towed.attachSearchRadius() + HITCH_QUERY_MARGIN),
                entity -> {
                    if (!(entity instanceof GeoVehicleEntity)) return false;
                    if (entity instanceof AbstractTrailerEntity) return false; // no chaining yet
                    ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
                    return id != null && towed.canBeTowedBy(id);
                });

        Entity best = null;
        double bestDistSq = Double.MAX_VALUE;
        boolean sawTakenHitch = false;

        for (Entity candidate : candidates) {
            Vec3 hitch = getHitchWorldPos(candidate);
            if (hitch == null) continue;                 // no trailer_driver config
            double distSq = hitch.distanceToSqr(tongue);
            if (distSq > radiusSq) continue;             // hitch out of reach of the tongue
            if (isHitchTaken(candidate, this)) {
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
        if (isAttached()) return;
        super.lerpTo(x, y, z, yaw, pitch, steps, teleport);
    }

    // ── Interaction ─────────────────────────────────────────────────────────────

    private void syncTowDataFromConfig() {
        TrailerTowedData towed = getTowedData();
        if (towed == null) return;
        this.entityData.set(TOW_X, (float) towed.towX());
        this.entityData.set(TOW_Y, (float) towed.towY());
        this.entityData.set(TOW_Z, (float) towed.towZ());
        this.entityData.set(MAX_ART, towed.maxArticulation());
    }

    /** Fallback click radius around the tongue when no Interactive OBB is defined. */
    protected double hitchZoneRadius() {
        return 1.0;
    }

    private static final double OBB_CONTAINS_EPSILON = 0.01;

    /**
     * True when a click landed on the tongue. Preferred: an OBB with "Part": "Interactive"
     * in the vehicle JSON (Size = half-extents, Position = trailer-local); falls back to a
     * sphere around the tongue point. The hit vec is a point on an OBB SURFACE, so it's
     * nudged slightly inward before the containment test.
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
        return !hasZoneBox && isNearTonguePoint(vec);
    }

    private boolean isNearTonguePoint(Vec3 vec) {
        double theta = Math.toRadians(this.getYRot());
        double cos = Math.cos(theta), sin = Math.sin(theta);

        // Inverse of the yaw rotation used everywhere else.
        double lx = vec.x * cos + vec.z * sin;
        double lz = -vec.x * sin + vec.z * cos;

        Vec3 tow = getTowOffset();
        double dx = lx - tow.x, dy = vec.y - tow.y, dz = lz - tow.z;
        double r = hitchZoneRadius();
        return dx * dx + dy * dy + dz * dz <= r * r;
    }

    /** Trailers aren't mounted, so a plain body click opens the hold; the tongue zone is claimed first below. */
    @Override
    protected boolean opensHoldOnPlainClick() {
        return true;
    }

    /** Empty hand near the tongue hitches/unhitches; everything else PASSes to normal interaction. */
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