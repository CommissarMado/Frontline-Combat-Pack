package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.tools.OBB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;

/**
 * Two holes in SBW's collision for collision-box vehicles, closed here:
 * <ul>
 *   <li><b>Wall climbing.</b> SBW steps a hull up (UpStep) whenever it counts as on the ground, and its
 *       box ground check also counts a hull pressed against a wall face. A hull hanging on a wall then
 *       steps up again every tick and walks up any height. A step is only allowed while the hull
 *       really rests on ground ({@link #restsOnGround}).</li>
 *   <li><b>Turning through walls.</b> Turning on the spot is never collision-checked, so the hull's
 *       corners swing into a wall; once enough of it is inside, the push-out flips to the far side.
 *       A turn that leaves the hull deeper than {@link #MAX_WALL_DEPTH} in a wall is undone
 *       ({@link #wallDepth}).</li>
 * </ul>
 */
public final class WallGuard {

    /** A sample on the underside counts as resting if ground is this close below (or this far into it). */
    private static final double REST_GAP = 0.35, REST_SINK = 0.6;
    /** Underside samples (of 5) that must rest for a step up to be allowed. */
    private static final int MIN_RESTING = 2;
    /** Block faces this high above the underside or more are walls, not steps. */
    private static final double WALL_ABOVE = 1.0;
    /** How deep (blocks) the hull may sit in a wall: SBW itself tolerates up to 0.1. */
    public static final double MAX_WALL_DEPTH = 0.15;

    private WallGuard() {
    }

    /** True if at least {@link #MIN_RESTING} of the box's underside corners and centre sit on ground. */
    public static boolean restsOnGround(VehicleEntity vehicle, OBB obb) {
        Level level = vehicle.level();
        Vector3d[] ax = obb.getAxes();
        Vector3d c = obb.center, e = obb.extents();
        double[][] at = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}, {0, 0}};
        int resting = 0;
        for (double[] f : at) {
            double lx = f[0] * e.x, ly = -e.y, lz = f[1] * e.z;
            double wx = c.x + ax[0].x * lx + ax[1].x * ly + ax[2].x * lz;
            double wy = c.y + ax[0].y * lx + ax[1].y * ly + ax[2].y * lz;
            double wz = c.z + ax[0].z * lx + ax[1].z * ly + ax[2].z * lz;
            Double top = groundTop(level, wx, wy, wz);
            if (top != null && wy - top <= REST_GAP && top - wy <= REST_SINK) resting++;
        }
        return resting >= MIN_RESTING;
    }

    /** Top of the highest collision shape in the column at (x, z) from just above y down to the rest gap. */
    private static Double groundTop(Level level, double x, double y, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int bx = (int) Math.floor(x), bz = (int) Math.floor(z);
        for (int by = (int) Math.floor(y + REST_SINK); by >= (int) Math.floor(y - REST_GAP); by--) {
            pos.set(bx, by, bz);
            VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
            if (!shape.isEmpty()) return by + shape.max(Direction.Axis.Y);
        }
        return null;
    }

    /** How deep (blocks) the box sits in blocks rising more than a step above its underside, sideways. */
    public static double wallDepth(VehicleEntity vehicle, OBB obb) {
        Level level = vehicle.level();
        AABB box = OBB.Companion.getWorldAABB(obb);
        double bottom = box.minY;
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
                        if (a.maxY <= bottom + WALL_ABOVE) continue;
                        Vector3d mtv = OBB.Companion.computeObbAabbMtv(obb, a);
                        if (mtv == null) continue;
                        double side = Math.sqrt(mtv.x * mtv.x + mtv.z * mtv.z);
                        if (side > Math.abs(mtv.y)) deepest = Math.max(deepest, side);
                    }
                }
            }
        }
        return deepest;
    }
}
