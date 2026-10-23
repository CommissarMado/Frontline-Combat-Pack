package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.data.vehicle.subdata.VehicleType;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Vector4d;

import java.util.List;

/**
 * Sets a landed helicopter down on its gear.
 * <p>
 * SBW's terrain tilt is not geometric: each TerrainCompat point pitches/rolls the vehicle by its height
 * above the ground times its distance from the centre, and the vehicle is never moved vertically (the
 * model just turns about a point RotateOffsetHeight above the entity). So gear that isn't modelled on
 * one plane - a raised tail wheel far behind the centre, tilted skids - gives a large wrong pitch and
 * leaves gear floating or buried.
 * <p>
 * For landed helicopters this replaces it: {@link #settle} solves the pitch and roll that best put every
 * gear point (the TerrainCompat points) onto the terrain under it - a small least-squares fit, eased in
 * over a few ticks - and {@link #solve} gives the vertical shift that then puts the lowest point exactly
 * on the ground. The shift is applied to the vehicle transform and the renderers (see the mixins), so
 * model, hitboxes and seats move together; the physics position is untouched.
 * <p>
 * Implemented on VehicleEntity by VehicleGroundContactMixin.
 */
public interface GroundContact {

    /** Vertical shift of the vehicle's pose in blocks (negative = down), interpolated. */
    float fcp$groundOffset(float partialTick);

    /** The farthest the pose is ever moved. */
    double MAX_SHIFT = 2.0;
    /** The steepest a landed helicopter is ever set, in degrees. */
    float MAX_TILT = 30f;

    static boolean applies(VehicleEntity v) {
        if (v.getVehicleType() != VehicleType.HELICOPTER || !v.onGround()) return false;
        List<Vec3> points = v.computed().getTerrainCompat();
        return points != null && points.size() >= 3;
    }

    /**
     * Turns the helicopter towards the attitude that puts all its gear points on the terrain.
     *
     * @return true if it handled the tilt (SBW's own terrain tilt is then skipped this tick)
     */
    static boolean settle(VehicleEntity v) {
        if (!applies(v)) return false;
        List<Vec3> points = v.computed().getTerrainCompat();
        float pitch = v.getXRot(), roll = v.getRoll();
        int n = points.size();
        double[] h = new double[n], g = new double[n], a = new double[n], b = new double[n];
        boolean[] ok = new boolean[n];
        int hits = 0;
        double d = 0.5; // degrees, finite-difference step
        for (int i = 0; i < n; i++) {
            Vec3 p = points.get(i);
            Vector4d w = pose(v, pitch, roll).transform(new Vector4d(p.x, p.y, p.z, 1));
            Double ground = groundBelow(v, w.x, w.y, w.z);
            if (ground == null) continue;
            ok[i] = true;
            hits++;
            h[i] = w.y;
            g[i] = ground;
            a[i] = (pose(v, pitch + (float) d, roll).transform(new Vector4d(p.x, p.y, p.z, 1)).y - w.y) / d;
            b[i] = (pose(v, pitch, roll + (float) d).transform(new Vector4d(p.x, p.y, p.z, 1)).y - w.y) / d;
        }
        if (hits < 3) return false;

        // Least squares for (dPitch, dRoll, shift): h + a*dp + b*dr + s = ground, with a tiny ridge on
        // the angles so near-degenerate layouts stay stable.
        double[][] m = new double[3][3];
        double[] r = new double[3];
        for (int i = 0; i < n; i++) {
            if (!ok[i]) continue;
            double[] row = {a[i], b[i], 1};
            double e = g[i] - h[i];
            for (int j = 0; j < 3; j++) {
                r[j] += row[j] * e;
                for (int k = 0; k < 3; k++) m[j][k] += row[j] * row[k];
            }
        }
        m[0][0] += 1e-4;
        m[1][1] += 1e-4;
        double[] sol = solve3(m, r);
        if (sol == null) return false;

        // Ease towards it: a damped step per tick, re-linearised every tick.
        float dp = (float) Mth.clamp(sol[0] * 0.5, -3, 3), dr = (float) Mth.clamp(sol[1] * 0.5, -3, 3);
        v.setXRot(Mth.clamp(pitch + dp, -MAX_TILT, MAX_TILT));
        v.setZRot(Mth.clamp(roll + dr, -MAX_TILT, MAX_TILT));
        return true;
    }

    /** Target vertical shift this tick: puts the lowest gear point on the ground; 0 unless landed. */
    static float solve(VehicleEntity v) {
        if (!applies(v)) return 0f;
        List<Vec3> points = v.computed().getTerrainCompat();
        Matrix4d t = pose(v, v.getXRot(), v.getRoll());
        double lowest = Double.MAX_VALUE;
        for (Vec3 local : points) {
            Vector4d w = t.transform(new Vector4d(local.x, local.y, local.z, 1));
            Double ground = groundBelow(v, w.x, w.y, w.z);
            if (ground != null) lowest = Math.min(lowest, w.y - ground);
        }
        if (lowest == Double.MAX_VALUE) return 0f;
        return (float) Mth.clamp(-lowest, -MAX_SHIFT, MAX_SHIFT);
    }

    /** SBW's vehicle transform without our shift: up to the pivot, yaw/pitch/roll, back down. */
    private static Matrix4d pose(VehicleEntity v, float pitch, float roll) {
        double h = v.getRotateOffsetHeight();
        return new Matrix4d()
                .translate(v.getX(), v.getY() + h, v.getZ())
                .rotate(Axis.YP.rotationDegrees(-v.getYRot()))
                .rotate(Axis.XP.rotationDegrees(pitch))
                .rotate(Axis.ZP.rotationDegrees(roll))
                .translate(0, -h, 0);
    }

    private static Double groundBelow(VehicleEntity v, double x, double y, double z) {
        BlockHitResult hit = v.level().clip(new ClipContext(new Vec3(x, y + MAX_SHIFT, z),
                new Vec3(x, y - MAX_SHIFT - 1, z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, v));
        return hit.getType() == HitResult.Type.MISS ? null : hit.getLocation().y;
    }

    private static double[] solve3(double[][] m, double[] r) {
        double det = det3(m);
        if (Math.abs(det) < 1e-12) return null;
        double[] out = new double[3];
        for (int c = 0; c < 3; c++) {
            double[][] mc = new double[3][];
            for (int i = 0; i < 3; i++) {
                mc[i] = m[i].clone();
                mc[i][c] = r[i];
            }
            out[c] = det3(mc) / det;
        }
        return out;
    }

    private static double det3(double[][] m) {
        return m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])
                - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
                + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
    }
}
