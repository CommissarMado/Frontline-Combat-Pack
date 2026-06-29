package frontline.combat.fcp.client.model.Util;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.entity.vehicle.SteerableVehicle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WheelRotationTransforms — spins wheel bones from how far a vehicle actually moves,
 * mirroring {@link CannonRecoilTransforms}.
 *
 * <p>Reusable for ANY vehicle and ANY mix of wheel sizes. SBW only spins wheels for
 * engine-driven vehicles (via its own getWheelRotation()), so vehicles it doesn't
 * drive — trailers, towed or pushed entities, anything custom — get no wheel motion.
 * This utility instead derives the rolling angle from the vehicle's per-tick travel,
 * so it needs nothing added to the entity. All state is kept here, client-side,
 * keyed weakly by entity so it cleans itself up.
 *
 * <p>Add it in a model's collectTransform(boneName), exactly like recoil:
 * <pre>
 *   {@literal @Override}
 *   public VehicleModel.TransformContext{@literal <MyEntity>} collectTransform(String boneName) {
 *       // Steering wheels (roll on X + pivot on Y):
 *       var turn = WheelRotationTransforms.matchAnyTurn(boneName, 0.5340, 30f,
 *               "WheelL0Turn", "WheelR0Turn");
 *       if (turn != null) return turn;
 *
 *       // Plain rolling wheels:
 *       var wheels = WheelRotationTransforms.matchAny(boneName, 0.5340,
 *               "WheelL0", "WheelR0", "WheelL1", "WheelR1");
 *       if (wheels != null) return wheels;
 *
 *       return super.collectTransform(boneName);
 *   }
 * </pre>
 *
 * <h2>Radius — what number to pass</h2>
 * The radius is the wheel's <b>rolling (outer) radius in blocks</b>, so the tread
 * matches the ground with no visible sliding. Blockbench geometry is authored in
 * <b>pixels</b> (the small grid unit), where {@value #PIXELS_PER_BLOCK} px = 1 block
 * = 1 metre. So:
 * <pre>
 *   radius(blocks) = tyrePixelDiameter / 2 / 16   =   tyrePixelDiameter / 32
 * </pre>
 * To read it off the model: select the tyre bone in Blockbench, take its vertical
 * height in pixels (top of tread to bottom of tread = the diameter) and divide by 32.
 * If you'd rather pass the pixel value directly, use {@link #fromPixels(double)}:
 * <pre>
 *   WheelRotationTransforms.matchAny(boneName,
 *           WheelRotationTransforms.fromPixels(8.54),   // 8.54 px radius -> 0.5338 blocks
 *           "WheelL0", "WheelR0");
 * </pre>
 *
 * <h2>Mixed wheel sizes</h2>
 * The internal accumulator stores signed <i>distance travelled</i> (radius-independent),
 * and each bone converts that to its own angle from its own radius at sample time. A
 * vehicle can therefore have large rear wheels and small front wheels and both stay
 * correct — just pass each set the right radius.
 *
 * <h2>Steering source</h2>
 * "...Turn" bones pivot on Y from the vehicle's steering angle. The angle is resolved,
 * in order: (1) {@link SteerableVehicle} if the entity implements it (preferred,
 * zero-reflection); (2) otherwise a cached reflective lookup of public
 * {@code getSteeringAngle()/getPrevSteeringAngle()} so vehicles that expose the
 * getters but don't declare the interface still steer; (3) otherwise 0 (straight).
 */
public final class WheelRotationTransforms {

    /** Blockbench grid units per block (1 px = 1/16 block = 1/16 m). */
    public static final double PIXELS_PER_BLOCK = 16.0;

    /** Wheel radius in blocks used by the no-radius overloads. */
    public static final double DEFAULT_RADIUS = 0.5;

    /** Default steering clamp in degrees. */
    public static final float DEFAULT_MAX_STEER = 30f;

    /** Axis the wheel bone rolls around. Most GeckoLib wheels roll on X. */
    public enum SpinAxis { X, Y, Z }

    /** Guards against teleports/dimension hops producing a giant one-tick spin. */
    private static final double MAX_TICK_TRAVEL = 8.0; // blocks per tick

    private static final double DEG_PER_RAD = 180.0 / Math.PI;

    private WheelRotationTransforms() {
    }

    // ── Unit helper ────────────────────────────────────────────────────────────

    /** Converts a Blockbench pixel measurement to blocks ({@code px / 16}). */
    public static double fromPixels(double pixels) {
        return pixels / PIXELS_PER_BLOCK;
    }

    // ── Per-vehicle rolling state (client render thread only) ──────────────────

    private static final Map<Entity, State> STATES = new WeakHashMap<>();

    private static final class State {
        int lastTick = Integer.MIN_VALUE;
        double lastX, lastZ;
        // Signed distance the vehicle has rolled, in blocks (radius-independent).
        double distance, prevDistance;
        boolean primed;
    }

    /**
     * Advances accumulated travel once per game tick (guarded by tickCount, so it is
     * correct no matter how many render frames fall in a tick) and returns it
     * interpolated for the current partial tick. Sign is forward/reverse relative to
     * the vehicle's facing.
     */
    private static double sampleDistance(VehicleEntity vehicle, float partialTick) {
        State s = STATES.computeIfAbsent(vehicle, k -> new State());

        if (vehicle.tickCount != s.lastTick) {
            s.lastTick = vehicle.tickCount;
            s.prevDistance = s.distance;

            if (s.primed) {
                double dx = vehicle.getX() - s.lastX;
                double dz = vehicle.getZ() - s.lastZ;
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 1.0e-5 && dist < MAX_TICK_TRAVEL) {
                    double yaw = Math.toRadians(vehicle.getYRot());
                    double forwardX = -Math.sin(yaw), forwardZ = Math.cos(yaw);
                    double sign = (dx * forwardX + dz * forwardZ) >= 0.0 ? 1.0 : -1.0;
                    s.distance += sign * dist;
                }
            }

            s.lastX = vehicle.getX();
            s.lastZ = vehicle.getZ();
            s.primed = true;
        }

        return Mth.lerp((double) partialTick, s.prevDistance, s.distance);
    }

    /** Rolling angle in degrees for a wheel of the given radius (blocks), wrapped to [-180,180]. */
    private static float rollDegrees(VehicleEntity vehicle, float partialTick, double radius) {
        double r = Math.max(0.05, radius); // avoid div-by-zero / absurd spin
        double deg = sampleDistance(vehicle, partialTick) / r * DEG_PER_RAD;
        // Wrap in double before narrowing so long sessions don't lose float precision.
        deg -= 360.0 * Math.floor(deg / 360.0 + 0.5);
        return (float) deg;
    }

    private static void applyRoll(CoreGeoBone bone, SpinAxis axis, float deg, boolean invert) {
        float rad = (float) Math.toRadians(invert ? -deg : deg);
        switch (axis) {
            case X -> bone.setRotX(rad);
            case Y -> bone.setRotY(rad);
            case Z -> bone.setRotZ(rad);
        }
    }

    // ── Public API: plain rolling wheels ───────────────────────────────────────

    @Nullable
    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> match(
            String boneName, String wheelBone) {
        return match(boneName, wheelBone, DEFAULT_RADIUS, SpinAxis.X, true);
    }

    @Nullable
    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> match(
            String boneName, String wheelBone, double radius) {
        return match(boneName, wheelBone, radius, SpinAxis.X, true);
    }

    /**
     * @param radius wheel radius in blocks (matches tread speed to ground speed)
     * @param axis   bone axis to rotate; usually X
     * @param invert flip spin direction if the model's wheel bone is mirrored
     */
    @Nullable
    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> match(
            String boneName, String wheelBone, double radius, SpinAxis axis, boolean invert) {
        if (!wheelBone.equals(boneName)) {
            return null;
        }
        return (bone, vehicle, state) ->
                applyRoll(bone, axis, rollDegrees(vehicle, state.getPartialTick(), radius), invert);
    }

    /** Match any of several wheel bones (same radius, X axis, inverted). */
    @Nullable
    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> matchAny(
            String boneName, double radius, String... wheelBones) {
        for (String wb : wheelBones) {
            if (wb.equals(boneName)) {
                return match(boneName, wb, radius);
            }
        }
        return null;
    }

    /** Match any of several wheel bones using {@link #DEFAULT_RADIUS}. */
    @Nullable
    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> matchAny(
            String boneName, String... wheelBones) {
        return matchAny(boneName, DEFAULT_RADIUS, wheelBones);
    }

    // ── Steering ("...Turn" bones: roll on X + pivot on Y) ─────────────────────

    /** Supplies a steering angle (degrees) for a vehicle at a given partial tick. */
    @FunctionalInterface
    public interface SteeringSupplier<T extends VehicleEntity> {
        float steeringDegrees(T vehicle, float partialTick);
    }

    // Cached reflective access to getSteeringAngle()/getPrevSteeringAngle() per class,
    // so vehicles that expose the getters but don't declare SteerableVehicle still steer.
    private record SteerAccessor(@Nullable Method current, @Nullable Method prev) {
        static final SteerAccessor NONE = new SteerAccessor(null, null);

        boolean usable() {
            return current != null && prev != null;
        }
    }

    private static final Map<Class<?>, SteerAccessor> STEER_ACCESSORS = new ConcurrentHashMap<>();

    private static SteerAccessor accessorFor(Class<?> cls) {
        return STEER_ACCESSORS.computeIfAbsent(cls, c -> {
            try {
                Method cur = c.getMethod("getSteeringAngle");
                Method prv = c.getMethod("getPrevSteeringAngle");
                if (cur.getReturnType() == float.class && prv.getReturnType() == float.class) {
                    cur.setAccessible(true);
                    prv.setAccessible(true);
                    return new SteerAccessor(cur, prv);
                }
            } catch (ReflectiveOperationException ignored) {
                // No matching getters on this class — fall through to NONE.
            }
            return SteerAccessor.NONE;
        });
    }

    /** Resolves a steering angle: interface first, then reflective getters, else 0. */
    private static float steerableAngle(VehicleEntity vehicle, float partialTick) {
        if (vehicle instanceof SteerableVehicle s) {
            return Mth.lerp(partialTick, s.getPrevSteeringAngle(), s.getSteeringAngle());
        }
        SteerAccessor a = accessorFor(vehicle.getClass());
        if (a.usable()) {
            try {
                float cur = (float) a.current().invoke(vehicle);
                float prev = (float) a.prev().invoke(vehicle);
                return Mth.lerp(partialTick, prev, cur);
            } catch (ReflectiveOperationException ignored) {
                // Shouldn't happen once cached; treat as straight.
            }
        }
        return 0f;
    }

    /**
     * Match a steering wheel bone: it rolls on X (from travel) AND pivots on Y (from
     * the vehicle's steering angle). Steering is resolved via {@link SteerableVehicle}
     * or, failing that, the entity's {@code getSteeringAngle()} getters.
     */
    @Nullable
    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> matchTurn(
            String boneName, String turnBone, double radius, float maxSteerDeg) {
        return matchTurn(boneName, turnBone, radius, maxSteerDeg,
                WheelRotationTransforms::steerableAngle);
    }

    /**
     * As above, but with an explicit steering source — use this when a vehicle steers
     * the wrong way (flip the sign) or reads steering from somewhere non-standard:
     * <pre>
     *   WheelRotationTransforms.matchTurn(boneName, "WheelL0Turn", 0.5340, 30f,
     *       (v, pt) -> -Mth.lerp(pt, v.getPrevSteeringAngle(), v.getSteeringAngle()));
     * </pre>
     */
    @Nullable
    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> matchTurn(
            String boneName, String turnBone, double radius, float maxSteerDeg,
            SteeringSupplier<T> steering) {
        if (!turnBone.equals(boneName)) {
            return null;
        }
        return (bone, vehicle, state) -> {
            float partialTick = state.getPartialTick();

            // Roll about the axle (same convention as plain wheels: inverted X).
            applyRoll(bone, SpinAxis.X, rollDegrees(vehicle, partialTick, radius), true);

            // Pivot about vertical Y for steering. Roll is rotationally symmetric, so
            // applying it before the steer on the same bone is visually correct.
            float steer = Mth.clamp(
                    steering.steeringDegrees(vehicle, partialTick),
                    -maxSteerDeg, maxSteerDeg);
            bone.setRotY((float) Math.toRadians(steer));
        };
    }

    /** Match any of several "...Turn" bones (roll + steer). */
    @Nullable
    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> matchAnyTurn(
            String boneName, double radius, float maxSteerDeg, String... turnBones) {
        for (String tb : turnBones) {
            if (tb.equals(boneName)) {
                return matchTurn(boneName, tb, radius, maxSteerDeg);
            }
        }
        return null;
    }

    /** Match any "...Turn" bones using {@link #DEFAULT_RADIUS} and {@link #DEFAULT_MAX_STEER}. */
    @Nullable
    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> matchAnyTurn(
            String boneName, String... turnBones) {
        return matchAnyTurn(boneName, DEFAULT_RADIUS, DEFAULT_MAX_STEER, turnBones);
    }
}