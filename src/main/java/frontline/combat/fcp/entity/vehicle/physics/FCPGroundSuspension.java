package frontline.combat.fcp.entity.vehicle.physics;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Vector4d;

import java.util.List;

/**
 * FCPGroundSuspension (Build A — ground-follow, lift-only).
 *
 * Rests a vehicle on its own terrainCompat contact pads instead of relying purely on
 * the coarse bounding box. After SBW's normal movement has run, each pad is taken into
 * world space and a short downward raycast finds the ground under it. If any pad has
 * clipped BELOW the ground, the vehicle is lifted so its deepest pad sits on the
 * surface, downward velocity is cancelled, and onGround is set.
 *
 * This is the conservative first pass:
 *   - It only LIFTS (anti-sink / climb). Downhill is still gravity + the bounding box,
 *     which stays full-size as a safety floor — so vehicles never fall through and the
 *     render/cull box is unaffected.
 *   - It does nothing horizontal. Walls are still handled by SBW's move().
 * Build B removes the bounding box's role and owns both axes; this stays as the
 * functional rollback.
 *
 * Per vehicle, you tune terrain contact purely by placing the terrainCompat pads.
 */
public final class FCPGroundSuspension {

    /** How far above a pad to start the ground probe (max penetration we can recover). */
    private static final double PROBE_UP = 1.0;
    /** How far below a pad to keep probing, so we still find ground a pad rests just above. */
    private static final double PROBE_DOWN = 0.5;
    /** Lifts smaller than this are ignored (avoids micro-jitter). */
    private static final double EPS = 1.0e-3;

    private FCPGroundSuspension() {
    }

    /**
     * Lifts the vehicle so no terrainCompat pad is below the ground. Call server-side,
     * after super.baseTick(). Safe to call every tick; it no-ops when nothing penetrates.
     */
    public static void apply(VehicleEntity vehicle) {
        List<Vec3> pads = vehicle.computed().getTerrainCompat();
        if (pads == null || pads.isEmpty()) return;

        Matrix4d transform = vehicle.getWheelsTransform(1f);

        double maxPenetration = 0.0; // deepest a pad sits below its ground
        boolean foundGround = false;

        for (Vec3 pad : pads) {
            Vector4d w = vehicle.transformPosition(transform, pad.x, pad.y, pad.z);
            double px = w.x, py = w.y, pz = w.z;

            Vec3 from = new Vec3(px, py + PROBE_UP, pz);
            Vec3 to = new Vec3(px, py - PROBE_DOWN, pz);
            HitResult hit = vehicle.level().clip(new ClipContext(
                    from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, vehicle));
            if (hit.getType() == HitResult.Type.MISS) continue;

            foundGround = true;
            double groundY = hit.getLocation().y;
            double penetration = groundY - py; // > 0 means the pad is under the surface
            if (penetration > maxPenetration) {
                maxPenetration = penetration;
            }
        }

        if (!foundGround || maxPenetration <= EPS) return;

        // Lift so the deepest pad rests on its ground; the body stays level (tilt is
        // SBW's terrainCompact job), so this just sets resting height.
        vehicle.setPos(vehicle.getX(), vehicle.getY() + maxPenetration, vehicle.getZ());

        Vec3 dm = vehicle.getDeltaMovement();
        if (dm.y < 0) {
            vehicle.setDeltaMovement(new Vec3(dm.x, 0.0, dm.z));
        }
        vehicle.setOnGround(true);
    }
}