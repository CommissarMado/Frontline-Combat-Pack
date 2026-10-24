package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.data.vehicle.subdata.OBBInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4d;
import org.joml.Vector4d;

/**
 * Terrain tilt for vehicles with a collision box, as a rigid hull resting on the ground.
 * <p>
 * SBW fits ONE average slope through ground samples under the box. That averages away exactly the
 * cases where a hull should tip: perched on a block near its middle, the samples average to level, so
 * the tank sits balanced on the block in mid-air. Here the hull is a rigid plate under gravity: of all
 * pitch/roll attitudes that keep the box's underside on or above every ground sample, it takes the one
 * that puts the centre lowest - which tips it off the block until an end (or the hull) touches down.
 * On even or uniformly sloped ground that is the same as the fitted slope.
 * <p>
 * Only the target attitude changes; SBW's own smoothing (0.1 per tick) still eases the hull into it.
 */
public final class RigidTerrainRest {

    private static final int COLS = 5, ROWS = 9;
    /** Steepest attitude considered (tan 45 deg), as SBW clamps it. */
    private static final double MAX_SLOPE = 1.0;
    /** Weak pull towards the current attitude, so an exactly centred balance doesn't flicker. */
    private static final double HOLD = 1.0e-3;
    private static final float SMOOTHING = 0.1f;
    /**
     * Slope multiplier (x TerrainCompatRotateRate). SBW uses 2.5, but its slope is the leftover error
     * measured against the hull's current tilt, so the gain only speeds up convergence. This slope is the
     * ground itself, so anything above 1 over-tilts.
     */
    private static final double GAIN = 1.0;
    private static final float MAX_TILT = 45f;
    /** Tallest vertical face a vehicle can step: any taller is a wall. */
    private static final double MAX_STEP = 1.5;

    private RigidTerrainRest() {
    }

    /** Replaces SBW's slope-fit update; called where SBW would apply its fitted slope. */
    public static void apply(VehicleEntity vehicle) {
        OBBInfo box = vehicle.getCollisionOBBInfo();
        if (box == null) return;
        Vec3 c = box.getPosition(), s = box.getSize();
        double bottom = c.y - s.y;
        double searchUp = Math.min(vehicle.getStepHeight(), MAX_STEP), searchDown = Math.max(Math.max(s.x, s.y), s.z);
        Matrix4d flat = vehicle.getWheelsTransform(1f);
        Level level = vehicle.level();

        int n = COLS * ROWS, used = 0;
        // Ground under each sample relative to the flat underside; NaN = nothing to rest on (drop-off or wall).
        double[][] grid = new double[COLS][ROWS];
        for (int i = 0; i < COLS; i++) {
            double lx = c.x - s.x + 2 * s.x * i / (COLS - 1);
            for (int j = 0; j < ROWS; j++) {
                double lz = c.z - s.z + 2 * s.z * j / (ROWS - 1);
                Vector4d w = flat.transform(new Vector4d(lx, bottom, lz, 1));
                grid[i][j] = terrainTop(level, w.x, w.z, w.y + searchUp, w.y - searchDown) - w.y;
            }
        }

        // A sample standing more than MAX_STEP above a neighbour is a wall face the hull has nosed into
        // (SBW lets boxes sink ~0.1 into blocks), not ground: resting on it is what walked hulls up walls.
        double[] xs = new double[n], zs = new double[n], ts = new double[n];
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                double t = grid[i][j];
                if (Double.isNaN(t) || isWall(grid, i, j)) continue;
                xs[used] = 2 * s.x * i / (COLS - 1) - s.x;
                zs[used] = 2 * s.z * j / (ROWS - 1) - s.z;
                ts[used] = t;
                used++;
            }
        }
        if (used < 3) return;

        // Current attitude as ground slopes (ground rising forward = nose up = negative xRot).
        double gain = Math.max(vehicle.data().compute().getTerrainCompatRotateRate() * GAIN, 1.0e-3);
        double a0 = Math.tan(Math.toRadians(vehicle.getRoll())) / gain;
        double b0 = -Math.tan(Math.toRadians(vehicle.getXRot())) / gain;
        double[] best = minimise(xs, zs, ts, used, a0, b0);
        // Clamped to 45 deg as SBW does.
        float targetXRot = Mth.clamp((float) -Math.toDegrees(Math.atan(best[1] * gain)), -MAX_TILT, MAX_TILT);
        float targetRoll = Mth.clamp((float) Math.toDegrees(Math.atan(best[0] * gain)), -MAX_TILT, MAX_TILT);
        vehicle.setXRot(Mth.rotLerp(SMOOTHING, vehicle.getXRot(), targetXRot));
        vehicle.setZRot(Mth.rotLerp(SMOOTHING, vehicle.getRoll(), targetRoll));
    }

    /**
     * Height of the resting plane at the centre for slopes (a along x, b along z): the plane must stay
     * on or above every ground sample. Convex in (a, b), so nested ternary searches find its minimum.
     */
    private static double centreHeight(double[] xs, double[] zs, double[] ts, int n, double a, double b) {
        double h = -Double.MAX_VALUE;
        for (int i = 0; i < n; i++) h = Math.max(h, ts[i] - a * xs[i] - b * zs[i]);
        return h;
    }

    private static double objective(double[] xs, double[] zs, double[] ts, int n, double a, double b, double a0, double b0) {
        double da = a - a0, db = b - b0;
        return centreHeight(xs, zs, ts, n, a, b) + HOLD * (da * da + db * db);
    }

    private static double[] minimise(double[] xs, double[] zs, double[] ts, int n, double a0, double b0) {
        double lo = -MAX_SLOPE, hi = MAX_SLOPE;
        for (int it = 0; it < 40; it++) {
            double m1 = lo + (hi - lo) / 3, m2 = hi - (hi - lo) / 3;
            if (bestOverB(xs, zs, ts, n, m1, a0, b0)[1] <= bestOverB(xs, zs, ts, n, m2, a0, b0)[1]) hi = m2; else lo = m1;
        }
        double a = (lo + hi) / 2;
        return new double[]{a, bestOverB(xs, zs, ts, n, a, a0, b0)[0]};
    }

    /** For a fixed roll slope a: {best pitch slope b, objective there}. */
    private static double[] bestOverB(double[] xs, double[] zs, double[] ts, int n, double a, double a0, double b0) {
        double lo = -MAX_SLOPE, hi = MAX_SLOPE;
        for (int it = 0; it < 40; it++) {
            double m1 = lo + (hi - lo) / 3, m2 = hi - (hi - lo) / 3;
            if (objective(xs, zs, ts, n, a, m1, a0, b0) <= objective(xs, zs, ts, n, a, m2, a0, b0)) hi = m2; else lo = m1;
        }
        double b = (lo + hi) / 2;
        return new double[]{b, objective(xs, zs, ts, n, a, b, a0, b0)};
    }

    private static boolean isWall(double[][] grid, int i, int j) {
        double t = grid[i][j];
        int[][] near = {{i - 1, j}, {i + 1, j}, {i, j - 1}, {i, j + 1}};
        for (int[] p : near) {
            if (p[0] < 0 || p[0] >= COLS || p[1] < 0 || p[1] >= ROWS) continue;
            double o = grid[p[0]][p[1]];
            if (!Double.isNaN(o) && t - o > MAX_STEP) return true;
        }
        return false;
    }

    /**
     * Top of the ground in the column at (x, z) between fromY and toY, or NaN when there is none, or when
     * the column is still solid above fromY - a wall taller than the vehicle can step, not ground.
     */
    private static double terrainTop(Level level, double x, double z, double fromY, double toY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int bx = Mth.floor(x), bz = Mth.floor(z);
        double fx = x - bx, fz = z - bz;
        for (int by = Mth.floor(fromY) + 1; by >= Mth.floor(toY); by--) {
            pos.set(bx, by, bz);
            VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
            if (shape.isEmpty()) continue;
            // Highest point of the shape over this exact spot (slabs, stairs, partial blocks).
            double top = -1;
            for (var aabb : shape.toAabbs()) {
                if (fx >= aabb.minX && fx <= aabb.maxX && fz >= aabb.minZ && fz <= aabb.maxZ) top = Math.max(top, aabb.maxY);
            }
            if (top < 0) top = shape.max(Direction.Axis.Y); // spot falls in a gap of the shape: use its top
            return by + top > fromY ? Double.NaN : by + top;
        }
        return Double.NaN;
    }
}
