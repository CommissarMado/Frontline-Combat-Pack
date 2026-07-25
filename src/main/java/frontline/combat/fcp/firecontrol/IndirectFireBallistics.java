package frontline.combat.fcp.firecontrol;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import com.atsuishio.superbwarfare.tools.TrajectoryCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class IndirectFireBallistics {

    public static final int MAX_RADIUS = 99;
    public static final int RANGE_TABLE_ROWS = 8;
    private static final double ANGLE_EPSILON = 0.05;

    private IndirectFireBallistics() {
    }

    public static FireControlComputation solve(
            VehicleEntity vehicle,
            int seatIndex,
            BlockPos target,
            TrajectoryMode mode
    ) {
        return solve(vehicle, seatIndex, target.getCenter(), mode);
    }

    public static FireControlComputation solve(
            VehicleEntity vehicle,
            int seatIndex,
            Vec3 target,
            TrajectoryMode mode
    ) {
        double velocity = vehicle.getProjectileVelocity(seatIndex);
        double gravity = vehicle.getProjectileGravity(seatIndex);
        if (velocity <= 0 || gravity <= 0 || vehicle.getGunData(seatIndex) == null) {
            return FireControlComputation.failure(FireControlStatus.INVALID_WEAPON);
        }

        Vec3 muzzle = vehicle.getShootPos(seatIndex, 1.0f);
        double distance = muzzle.distanceTo(target);
        Vec3 adjustedTarget = target.add(0, -1.0 - 0.0015 * distance, 0);
        Vec3 direction = TrajectoryCalculator.calculateLaunchVector(
                muzzle,
                adjustedTarget,
                velocity,
                gravity,
                mode == TrajectoryMode.LOW
        );
        if (direction == null || direction.lengthSqr() < 1.0E-8) {
            return FireControlComputation.failure(FireControlStatus.OUT_OF_RANGE);
        }

        direction = direction.normalize();
        double pitch = VehicleVecUtils.getXRotFromVector(direction);
        if (pitch + ANGLE_EPSILON < vehicle.getTurretMinPitch()
                || pitch - ANGLE_EPSILON > vehicle.getTurretMaxPitch()) {
            return FireControlComputation.failure(FireControlStatus.PITCH_LIMIT);
        }

        double forwardYaw = VehicleVecUtils.getYRotFromVector(vehicle.getForward());
        double desiredYaw = VehicleVecUtils.getYRotFromVector(direction);
        double relativeYaw = Mth.wrapDegrees(desiredYaw - forwardYaw);
        if (relativeYaw + ANGLE_EPSILON < vehicle.getTurretMinYaw()
                || relativeYaw - ANGLE_EPSILON > vehicle.getTurretMaxYaw()) {
            return FireControlComputation.failure(FireControlStatus.YAW_LIMIT);
        }

        double dx = target.x - muzzle.x;
        double dz = target.z - muzzle.z;
        double range = Math.sqrt(dx * dx + dz * dz);
        double horizontalSpeed = velocity * Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        double flightTime = horizontalSpeed > 1.0E-8 ? range / horizontalSpeed : 0;
        double yaw = -VehicleVecUtils.getYRotFromVector(direction);

        return FireControlComputation.success(new FireControlSolution(
                muzzle, target, adjustedTarget, direction, range, pitch, yaw, flightTime
        ));
    }

    public static Vec3 sampleTarget(BlockPos center, int radius, RandomSource random) {
        Vec3 target = center.getCenter();
        if (radius <= 0) {
            return target;
        }

        double sampledRadius = radius * Math.sqrt(random.nextDouble());
        double angle = random.nextDouble() * Math.PI * 2.0;
        return target.add(sampledRadius * Math.cos(angle), 0, sampledRadius * Math.sin(angle));
    }

    public static double rangeAtPitch(
            double velocity,
            double gravity,
            double muzzleY,
            double targetY,
            double pitchDegrees
    ) {
        if (velocity <= 0 || gravity <= 0 || pitchDegrees <= 0) {
            return 0;
        }

        double pitch = Math.toRadians(pitchDegrees);
        double verticalSpeed = velocity * Math.sin(pitch);
        double discriminant = verticalSpeed * verticalSpeed - 2.0 * gravity * (targetY - muzzleY);
        if (discriminant < 0) {
            return 0;
        }

        double flightTime = (verticalSpeed + Math.sqrt(discriminant)) / gravity;
        return velocity * Math.cos(pitch) * flightTime;
    }
}
