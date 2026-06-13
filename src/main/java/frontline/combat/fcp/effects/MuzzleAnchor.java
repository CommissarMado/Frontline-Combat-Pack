package frontline.combat.fcp.effects;

import com.atsuishio.superbwarfare.data.gun.ShootParameters;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record MuzzleAnchor(int vehicleId, int seatIndex) {

    public static final MuzzleAnchor NONE = new MuzzleAnchor(-1, 0);

    public boolean isValid() {
        return vehicleId >= 0;
    }

    public static MuzzleAnchor from(ShootParameters parameters) {
        if (!(parameters.ammoSupplier instanceof VehicleEntity vehicle)) {
            return NONE;
        }
        int seat = parameters.shooter != null ? vehicle.getSeatIndex(parameters.shooter) : 0;
        return new MuzzleAnchor(vehicle.getId(), seat);
    }

    public MuzzlePositionResolver.MuzzlePose resolve(Level level) {
        if (!isValid()) {
            return null;
        }
        Entity entity = level.getEntity(vehicleId);
        if (!(entity instanceof VehicleEntity vehicle)) {
            MuzzleBurstTracker.clear(vehicleId);
            return null;
        }
        Vec3 position = vehicle.getShootPos(seatIndex, 1f);
        Vec3 direction = vehicle.getShootVec(seatIndex, 1f);
        if (direction == null || direction.lengthSqr() < 1.0E-8) {
            direction = vehicle.getViewVector(1f);
        } else {
            direction = direction.normalize();
        }
        return new MuzzlePositionResolver.MuzzlePose(position, direction);
    }
}
