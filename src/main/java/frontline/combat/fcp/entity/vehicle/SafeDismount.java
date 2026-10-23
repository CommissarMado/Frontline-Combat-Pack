package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.tools.OBB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Makes sure a passenger never leaves a vehicle into or under the ground, through a wall, or far away.
 * <p>
 * Two things put players into the ground. SBW places seats that have a "DismountInfo" position at that
 * point as-is (transformed by the vehicle's current pose, no check), so on a slope or a tilted hull it can
 * be inside the terrain. And SBW's default exit spot is offset by the entity's registered size (3x2 for
 * most vehicles), not its real hitboxes, so on big vehicles it lands inside the hull - and SBW then
 * pushes entities out of its hitboxes along the shortest axis, which can be straight down.
 * <p>
 * A spot is accepted only if the player's standing box hits no blocks and none of the vehicle's
 * hitboxes, and there are no blocks between it and both the passenger's eyes and the vehicle's origin
 * (so never through a wall, even with part of the vehicle clipped into one).
 * Candidates are tried nearest first: the intended spot, small steps around it, then just outside the
 * hull next to it; as a last resort the roof.
 */
public final class SafeDismount {

    /** Highest a spot is lifted above the ground level it was meant for, in blocks. */
    private static final double MAX_RISE = 1.5;
    /** Furthest a spot is moved sideways from the intended one before trying the hull's edge. */
    private static final double MAX_SHIFT = 1.5;
    private static final double STEP = 0.25;

    private SafeDismount() {
    }

    /**
     * @param grounded true for a normal dismount: a spot below the base of a vehicle standing on the
     *                 ground is underground by definition, even if it happens to be in an air pocket.
     *                 False for ejection, which happens in the air.
     */
    public static Vec3 vet(VehicleEntity vehicle, LivingEntity passenger, Vec3 wanted, boolean grounded) {
        // A spot must be reachable from the passenger and from the vehicle's own origin (its 0 0 0), so
        // neither a seat nor a hull poking through a wall lets anyone out on the other side.
        Vec3[] from = {passenger.getEyePosition(), vehicle.position()};
        Vec3 start = wanted;
        if (grounded && vehicle.onGround() && start.y < vehicle.getY()) {
            start = new Vec3(start.x, vehicle.getY(), start.z);
        }
        if (start == wanted && fits(vehicle, passenger, from, wanted)) return wanted;

        // Around the intended spot, nearest first.
        Vec3 found = nearest(vehicle, passenger, from, start, around(start));
        if (found != null) return found;

        // Just outside the hull, at the points of its outline closest to the intended spot.
        AABB body = hitboxBounds(vehicle);
        double gap = passenger.getBbWidth() / 2 + 0.3, y = vehicle.getY();
        double cx = clamp(start.x, body.minX, body.maxX), cz = clamp(start.z, body.minZ, body.maxZ);
        List<Vec3> edge = new ArrayList<>(List.of(
                new Vec3(body.minX - gap, y, cz), new Vec3(body.maxX + gap, y, cz),
                new Vec3(cx, y, body.minZ - gap), new Vec3(cx, y, body.maxZ + gap)));
        for (Vec3 e : List.copyOf(edge)) edge.addAll(around(e));
        found = nearest(vehicle, passenger, from, start, edge);
        if (found != null) return found;

        return new Vec3(vehicle.getX(), body.maxY, vehicle.getZ());
    }

    /** Horizontal offsets of the spot up to MAX_SHIFT, each at heights up to MAX_RISE. */
    private static List<Vec3> around(Vec3 c) {
        List<Vec3> out = new ArrayList<>();
        for (double dx = -MAX_SHIFT; dx <= MAX_SHIFT + 1e-9; dx += STEP)
            for (double dz = -MAX_SHIFT; dz <= MAX_SHIFT + 1e-9; dz += STEP)
                if (dx * dx + dz * dz <= MAX_SHIFT * MAX_SHIFT + 1e-9)
                    for (double dy = 0; dy <= MAX_RISE + 1e-9; dy += STEP) out.add(c.add(dx, dy, dz));
        return out;
    }

    private static Vec3 nearest(VehicleEntity vehicle, LivingEntity passenger, Vec3[] from, Vec3 to, List<Vec3> candidates) {
        candidates.sort(Comparator.comparingDouble(p -> p.distanceToSqr(to)));
        for (Vec3 p : candidates) if (fits(vehicle, passenger, from, p)) return p;
        return null;
    }

    private static boolean fits(VehicleEntity vehicle, LivingEntity passenger, Vec3[] from, Vec3 pos) {
        if (!DismountHelper.canDismountTo(vehicle.level(), pos, passenger, Pose.STANDING)) return false;
        AABB box = passenger.getLocalBoundsForPose(Pose.STANDING).move(pos);
        for (OBB obb : hitboxes(vehicle)) {
            if (OBB.isColliding(obb, box)) return false;
        }
        // Reachable: no blocks between each origin (passenger's eyes, vehicle origin) and the spot's body.
        Vec3 body = pos.add(0, box.getYsize() * 0.5, 0);
        for (Vec3 origin : from) {
            if (vehicle.level().clip(new ClipContext(origin, body, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, passenger))
                    .getType() != HitResult.Type.MISS) return false;
        }
        return true;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static List<OBB> hitboxes(VehicleEntity vehicle) {
        List<OBB> obbs = vehicle.getOBBs();
        return obbs == null ? List.of() : obbs;
    }

    /** World bounds of all the vehicle's hitboxes (falls back to its bounding box). */
    private static AABB hitboxBounds(VehicleEntity vehicle) {
        double x0 = Double.MAX_VALUE, y0 = Double.MAX_VALUE, z0 = Double.MAX_VALUE;
        double x1 = -Double.MAX_VALUE, y1 = -Double.MAX_VALUE, z1 = -Double.MAX_VALUE;
        for (OBB obb : hitboxes(vehicle)) {
            for (Vector3d v : obb.getVertices()) {
                x0 = Math.min(x0, v.x); y0 = Math.min(y0, v.y); z0 = Math.min(z0, v.z);
                x1 = Math.max(x1, v.x); y1 = Math.max(y1, v.y); z1 = Math.max(z1, v.z);
            }
        }
        return x0 == Double.MAX_VALUE ? vehicle.getBoundingBox() : new AABB(x0, y0, z0, x1, y1, z1);
    }
}
