package frontline.combat.fcp.effects;

import net.minecraft.world.phys.Vec3;

/** Gun-local axes (forward / right / up) for muzzle particles. */
public record GunAxes(Vec3 forward, Vec3 right, Vec3 up) {

    public static GunAxes fromDirection(Vec3 forward) {
        Vec3 right = forward.cross(new Vec3(0, 1, 0));
        if (right.lengthSqr() < 1.0E-6) {
            right = forward.cross(new Vec3(1, 0, 0));
        }
        right = right.normalize();
        Vec3 up = right.cross(forward).normalize();
        return new GunAxes(forward, right, up);
    }

    public Vec3 toWorld(Vec3 local) {
        return right.scale(local.x).add(up.scale(local.y)).add(forward.scale(local.z));
    }
}
