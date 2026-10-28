package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.tools.OBB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;

/**
 * Collision-box vehicles: SBW never collision-checks turning on the spot, so a hull's corners can
 * swing into a wall, and once enough of it is inside, SBW's push-out flips it to the far side. Like a
 * vanilla entity stuck in a block, a hull inside a wall is pushed back out sideways every tick
 * ({@link #pushOutOfWalls}). Only blocks rising more than a step above the hull's underside right above
 * them count as walls (so a hull tilted up a slope or stairs isn't held back), and ground and steps are
 * left to SBW.
 */
public final class WallGuard {

    /** Block faces this high above the underside or more are walls, not steps. */
    private static final double WALL_ABOVE = 1.0;
    /** Depth (blocks) left alone: SBW's own collision tolerates up to 0.1. */
    private static final double TOLERANCE = 0.05;
    private static final int PASSES = 3;

    private WallGuard() {
    }

    /** Moves the vehicle sideways out of any wall its collision box is in. */
    public static void pushOutOfWalls(VehicleEntity vehicle) {
        for (int pass = 0; pass < PASSES; pass++) {
            OBB box = vehicle.getCollisionOBB();
            if (box == null) return;
            Vector3d push = deepestWallPush(vehicle, box);
            if (push == null) return;
            double depth = Math.sqrt(push.x * push.x + push.z * push.z);
            if (depth <= TOLERANCE) return;
            double k = (depth - TOLERANCE) / depth;
            vehicle.setPos(vehicle.getX() + push.x * k, vehicle.getY(), vehicle.getZ() + push.z * k);
            vehicle.updateOBB();
            vehicle.refreshBoundingBoxFromOBBs();
        }
    }

    /** The horizontal part of the largest sideways separation vector against a wall block, or null. */
    private static Vector3d deepestWallPush(VehicleEntity vehicle, OBB obb) {
        Level level = vehicle.level();
        AABB box = OBB.Companion.getWorldAABB(obb);
        Vector3d[] ax = obb.getAxes();
        Vector3d e = obb.extents(), up = ax[1];
        // Centre of the underside; "up" is the hull's own up axis, so the underside tilts with the hull.
        double ux = obb.center.x - up.x * e.y, uy = obb.center.y - up.y * e.y, uz = obb.center.z - up.z * e.y;
        double bottom = box.minY;
        Vector3d best = null;
        double deepest = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = (int) Math.floor(box.minX); x <= (int) Math.floor(box.maxX); x++) {
            for (int z = (int) Math.floor(box.minZ); z <= (int) Math.floor(box.maxZ); z++) {
                for (int y = (int) Math.floor(bottom + WALL_ABOVE); y <= (int) Math.floor(box.maxY); y++) {
                    pos.set(x, y, z);
                    VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
                    if (shape.isEmpty()) continue;
                    for (AABB part : shape.toAabbs()) {
                        AABB a = part.move(x, y, z);
                        // Compare with the underside right above this block, not the lowest corner: on a
                        // slope the raised nose sits well above the steps it is climbing.
                        double cx = (a.minX + a.maxX) / 2, cz = (a.minZ + a.maxZ) / 2;
                        double underside = Math.abs(up.y) < 1.0e-3 ? bottom
                                : uy - (up.x * (cx - ux) + up.z * (cz - uz)) / up.y;
                        if (a.maxY <= underside + WALL_ABOVE) continue;
                        Vector3d mtv = OBB.Companion.computeObbAabbMtv(obb, a);
                        if (mtv == null) continue;
                        double side = Math.sqrt(mtv.x * mtv.x + mtv.z * mtv.z);
                        if (side > Math.abs(mtv.y) && side > deepest) {
                            deepest = side;
                            best = new Vector3d(mtv.x, 0, mtv.z);
                        }
                    }
                }
            }
        }
        return best;
    }
}
