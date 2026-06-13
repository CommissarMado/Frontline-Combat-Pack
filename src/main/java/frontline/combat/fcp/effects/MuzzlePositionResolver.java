package frontline.combat.fcp.effects;

import com.atsuishio.superbwarfare.data.gun.ShootParameters;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Resolves world-space muzzle pose from SBW vehicle transforms (Barrel / Turret / etc.).
 */
public final class MuzzlePositionResolver {

    public record MuzzlePose(Vec3 position, Vec3 direction) {
    }

    private MuzzlePositionResolver() {
    }

    public static MuzzlePose resolve(ShootParameters parameters) {
        Vec3 position = parameters.shootPosition;
        Vec3 direction = parameters.shootDirection;

        Entity supplier = parameters.ammoSupplier;
        Entity shooter = parameters.shooter;
        if (supplier instanceof VehicleEntity vehicle) {
            if (shooter != null) {
                position = vehicle.getShootPos(shooter, 1f);
                direction = vehicle.getShootVec(shooter, 1f);
            } else {
                position = vehicle.getShootPos(0, 1f);
                Vec3 seatDir = vehicle.getShootVec(0, 1f);
                if (seatDir != null) {
                    direction = seatDir;
                }
            }
        }

        if (direction == null || direction.lengthSqr() < 1.0E-8) {
            direction = new Vec3(0, 0, 1);
        } else {
            direction = direction.normalize();
        }

        return new MuzzlePose(position, direction);
    }
}
