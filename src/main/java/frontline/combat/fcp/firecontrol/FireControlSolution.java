package frontline.combat.fcp.firecontrol;

import net.minecraft.world.phys.Vec3;

public record FireControlSolution(
        Vec3 muzzle,
        Vec3 target,
        Vec3 adjustedTarget,
        Vec3 direction,
        double range,
        double pitch,
        double yaw,
        double flightTime
) {
}
