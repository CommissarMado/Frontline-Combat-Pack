package frontline.combat.fcp.client.particle;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class BarrelMuzzleTracking {

    private static final float REFERENCE_RECOIL_TICKS = 42f;
    private static final float SLIDE_MAX = 12f;
    private static final float KICK_MAX_DEG = 3f;
    /** GeckoLib bone units → blocks (same scale as {@code CannonRecoilTransforms}). */
    private static final float GEO_TO_BLOCK = 1f / 16f;

    private BarrelMuzzleTracking() {
    }

    public static Vec3 resolveMuzzle(VehicleEntity vehicle, int seatIndex) {
        Vec3 muzzle = vehicle.getShootPos(seatIndex, 1f);
        Vec3 forward = vehicle.getShootVec(seatIndex, 1f);
        if (forward == null || forward.lengthSqr() < 1.0E-8) {
            forward = vehicle.getViewVector(1f);
        } else {
            forward = forward.normalize();
        }
        return applyVisualRecoilOffset(vehicle, muzzle, forward);
    }

    /**
     * @return {@code true} if the particle position was updated; {@code false} if tracking was frozen this tick
     */
    public static boolean applyFollowDelta(
            VehicleEntity vehicle,
            int seatIndex,
            double[] lastMuzzle,
            double[] position,
            float smoothing
    ) {
        if (vehicle.getCannonRecoilTime() > 0) {
            Vec3 muzzle = resolveMuzzle(vehicle, seatIndex);
            lastMuzzle[0] = muzzle.x;
            lastMuzzle[1] = muzzle.y;
            lastMuzzle[2] = muzzle.z;
            return false;
        }

        Vec3 muzzle = resolveMuzzle(vehicle, seatIndex);
        if (Double.isNaN(lastMuzzle[0])) {
            lastMuzzle[0] = muzzle.x;
            lastMuzzle[1] = muzzle.y;
            lastMuzzle[2] = muzzle.z;
            return false;
        }

        double dx = (muzzle.x - lastMuzzle[0]) * smoothing;
        double dy = (muzzle.y - lastMuzzle[1]) * smoothing;
        double dz = (muzzle.z - lastMuzzle[2]) * smoothing;

        position[0] += dx;
        position[1] += dy;
        position[2] += dz;

        lastMuzzle[0] = muzzle.x;
        lastMuzzle[1] = muzzle.y;
        lastMuzzle[2] = muzzle.z;
        return true;
    }

    private static Vec3 applyVisualRecoilOffset(VehicleEntity vehicle, Vec3 muzzle, Vec3 forward) {
        int recoilTime = vehicle.getCannonRecoilTime();
        if (recoilTime <= 0) {
            return muzzle;
        }

        float force = Mth.clamp(vehicle.getCannonRecoilForce(), 0f, 2f);
        float progress = recoilTime / REFERENCE_RECOIL_TICKS;
        float slide = force * SLIDE_MAX * progress * progress * GEO_TO_BLOCK;
        float kick = force * KICK_MAX_DEG * progress * progress
                * (float) Math.sin(0.2 * Math.PI * (recoilTime - 2.5))
                * Mth.DEG_TO_RAD;

        Vec3 right = forward.cross(new Vec3(0, 1, 0));
        if (right.lengthSqr() < 1.0E-6) {
            right = forward.cross(new Vec3(1, 0, 0));
        }
        right = right.normalize();
        Vec3 up = right.cross(forward).normalize();

        return muzzle
                .subtract(forward.scale(slide))
                .add(up.scale(slide * kick * 4f));
    }
}