package frontline.combat.fcp.client.model.Util;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.entity.vehicle.SteerableVehicle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * WheelRotationTransforms — spins wheel bones from how far a vehicle actually moves,
 * mirroring {@link CannonRecoilTransforms}.
 *
 * Reusable for ANY vehicle. SBW only spins wheels for engine-driven vehicles (via
 * its own getWheelRotation()), so vehicles it doesn't drive — trailers, towed or
 * pushed entities, anything custom — get no wheel motion. This utility instead
 * derives the rolling angle from the vehicle's per-tick travel (distance / wheel
 * circumference), so it needs nothing added to the entity. All state is kept here,
 * client-side, keyed weakly by entity so it cleans itself up.
 *
 * Add it in a model's collectTransform(boneName), exactly like recoil:
 * <pre>
 *   {@literal @Override}
 *   public VehicleModel.TransformContext{@literal <MyEntity>} collectTransform(String boneName) {
 *       var wheels = WheelRotationTransforms.matchAny(boneName, 0.5,
 *               "WheelL0", "WheelR0", "WheelL1", "WheelR1");
 *       if (wheels != null) return wheels;
 *       return super.collectTransform(boneName);
 *   }
 * </pre>
 *
 * The radius is the model's wheel radius in blocks (so the tread matches the ground
 * with no visible sliding). The default helpers use {@link #DEFAULT_RADIUS}.
 */
public final class WheelRotationTransforms {

    /** Wheel radius in blocks used by the no-radius overloads. */
    public static final double DEFAULT_RADIUS = 0.5;

    /** Axis the wheel bone rolls around. Most GeckoLib wheels roll on X. */
    public enum SpinAxis { X, Y, Z }

    private WheelRotationTransforms() {
    }

    // ── Per-vehicle rolling state (client render thread only) ──────────────────

    private static final Map<Entity, State> STATES = new WeakHashMap<>();

    private static final class State {
        int lastTick = Integer.MIN_VALUE;
        double lastX, lastZ;
        float angle, prevAngle;
        boolean primed;
    }

    /**
     * Advances the angle once per game tick (guarded by tickCount, so it is correct
     * no matter how many render frames fall in a tick) and returns it interpolated
     * for the current partial tick.
     */
    private static float sampleAngle(VehicleEntity vehicle, float partialTick, double radius) {
        State s = STATES.computeIfAbsent(vehicle, k -> new State());

        if (vehicle.tickCount != s.lastTick) {
            s.lastTick = vehicle.tickCount;
            s.prevAngle = s.angle;

            if (s.primed) {
                double dx = vehicle.getX() - s.lastX;
                double dz = vehicle.getZ() - s.lastZ;
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 1.0e-5) {
                    double yaw = Math.toRadians(vehicle.getYRot());
                    double forwardX = -Math.sin(yaw), forwardZ = Math.cos(yaw);
                    double sign = (dx * forwardX + dz * forwardZ) >= 0.0 ? 1.0 : -1.0;
                    double safeRadius = Math.max(0.05, radius);
                    s.angle = Mth.wrapDegrees(
                            s.angle + (float) (sign * dist * (180.0 / (Math.PI * safeRadius))));
                }
            }

            s.lastX = vehicle.getX();
            s.lastZ = vehicle.getZ();
            s.primed = true;
        }

        return Mth.rotLerp(partialTick, s.prevAngle, s.angle);
    }

    // ── Public API (mirrors CannonRecoilTransforms.match) ──────────────────────

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
        float dir = invert ? -1f : 1f;
        return (bone, vehicle, state) -> {
            float deg = sampleAngle(vehicle, state.getPartialTick(), radius);
            float rad = (float) Math.toRadians(dir * deg);
            switch (axis) {
                case X -> bone.setRotX(rad);
                case Y -> bone.setRotY(rad);
                case Z -> bone.setRotZ(rad);
            }
        };
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

    /** Default steering clamp in degrees. */
    public static final float DEFAULT_MAX_STEER = 30f;

    /** Supplies a steering angle (degrees) for a vehicle at a given partial tick. */
    @FunctionalInterface
    public interface SteeringSupplier<T extends VehicleEntity> {
        float steeringDegrees(T vehicle, float partialTick);
    }

    /** Reads steering from {@link SteerableVehicle}; 0 (straight) if not steerable. */
    private static float steerableAngle(VehicleEntity vehicle, float partialTick) {
        if (vehicle instanceof SteerableVehicle s) {
            return Mth.lerp(partialTick, s.getPrevSteeringAngle(), s.getSteeringAngle());
        }
        return 0f;
    }

    /**
     * Match a steering wheel bone: it rolls on X (from travel) AND pivots on Y (from
     * the vehicle's steering angle). Steering comes from {@link SteerableVehicle};
     * vehicles that don't implement it keep the wheel straight.
     */
    @Nullable
    public static <T extends VehicleEntity & GeoAnimatable> VehicleModel.TransformContext<T> matchTurn(
            String boneName, String turnBone, double radius, float maxSteerDeg) {
        return matchTurn(boneName, turnBone, radius, maxSteerDeg,
                WheelRotationTransforms::steerableAngle);
    }

    /**
     * As above, but with an explicit steering source — use this when the vehicle's
     * steering getters aren't exposed through {@link SteerableVehicle}:
     * <pre>
     *   WheelRotationTransforms.matchTurn(boneName, "WheelL0Turn", 0.5f, 30f,
     *       (v, pt) -> Mth.lerp(pt, v.getPrevSteeringAngle(), v.getSteeringAngle()));
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
            float roll = sampleAngle(vehicle, state.getPartialTick(), radius);
            bone.setRotX((float) Math.toRadians(-roll));

            float steer = Mth.clamp(
                    steering.steeringDegrees(vehicle, state.getPartialTick()),
                    -maxSteerDeg, maxSteerDeg);
            bone.setRotY((float) Math.toRadians(steer));
        };
    }

    /** Match any of several "...Turn" bones (roll + steer via SteerableVehicle). */
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