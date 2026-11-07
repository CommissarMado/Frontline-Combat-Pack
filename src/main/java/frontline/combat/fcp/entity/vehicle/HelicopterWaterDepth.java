package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.team.VehicleTeamLock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Vector4d;

import java.util.List;

/**
 * FCP helicopters in water. SBW's ticking water damage starts once the bounding box is half under, and that box is built from every
 * hitbox, so touching the surface was enough. The damage now also needs every gear point (the TerrainCompat points) DEPTH under
 * the surface. SBW's slowdown in water is left as it is.
 */
public final class HelicopterWaterDepth {

    private static final double DEPTH = 0.75;

    private HelicopterWaterDepth() {
    }

    /** Always true off FCP's helicopters, which leaves SBW's own condition deciding. */
    public static boolean gearDeep(VehicleEntity vehicle) {
        if (!VehicleTeamLock.isFcpVehicle(vehicle)) return true;
        List<Vec3> points = vehicle.computed().getTerrainCompat();
        if (points.isEmpty()) return deep(vehicle, vehicle.getX(), vehicle.getY(), vehicle.getZ());
        Matrix4d pose = GroundContact.pose(vehicle, vehicle.getXRot(), vehicle.getRoll());
        for (Vec3 point : points) {
            Vector4d world = pose.transform(new Vector4d(point.x, point.y, point.z, 1));
            if (!deep(vehicle, world.x, world.y, world.z)) return false;
        }
        return true;
    }

    private static boolean deep(VehicleEntity vehicle, double x, double y, double z) {
        BlockPos pos = BlockPos.containing(x, y + DEPTH, z);
        FluidState fluid = vehicle.level().getFluidState(pos);
        return fluid.is(FluidTags.WATER) && pos.getY() + fluid.getHeight(vehicle.level(), pos) >= y + DEPTH;
    }
}
