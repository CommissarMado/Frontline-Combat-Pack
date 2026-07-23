package frontline.combat.fcp.client.model.Btr82at;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Btr82at.BTR82ATEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * Unlike every other vehicle geo in the pack, the BTR-82AT turret carries a bind-pose
 * rotation: "turret" is declared with "rotation": [0, 180, 0], and its whole subtree is
 * authored inside that flipped frame (which is why Ligths/gun1 have no [-180,0,180] of
 * their own, while group2 does - it cancels the parent flip). Two consequences:
 *
 * 1. SuperbWarfare's turret handler assigns rotY absolutely, which would wipe the 180
 *    bind and leave the turret facing backwards. Here the base transform runs first and
 *    the bind rotation is composed back on top of it.
 *
 * 2. Inside a 180 degree yaw the local X and Z axes are reversed, so barrel elevation and
 *    recoil come out backwards. Both are mirrored after the shared helper computes them.
 *
 * Everything else (wheels, wreck handling, hull) matches the base BTR-82 model.
 */
public class BTR82ATModel extends FCPVehicleModel<BTR82ATEntity> {

    private static final String CANNON_WEAPON = "Cannon";

    @Override
    public ResourceLocation getModelResource(BTR82ATEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/btr82at.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<BTR82ATEntity> collectTransform(String boneName) {
        if ("barrel".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float turretXRot = Mth.lerp(state.getPartialTick(), vehicle.getTurretXRotO(), vehicle.getTurretXRot());
                CannonRecoilTransforms.applyBarrelPitchAndRecoil(
                        bone, vehicle, turretXRot, CannonRecoilTransforms.Profile.FORWARDBACK, CANNON_WEAPON);
                // Mirror elevation and recoil slide into the flipped turret frame.
                bone.setRotX(-bone.getRotX());
                bone.setPosZ(-bone.getPosZ());
            };
        }

        VehicleModel.TransformContext<BTR82ATEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<BTR82ATEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        VehicleModel.TransformContext<BTR82ATEntity> base = super.collectTransform(boneName);

        if (base != null && "turret".equals(boneName)) {
            return (bone, vehicle, state) -> {
                base.transform(bone, vehicle, state);
                // Re-apply the bind-pose yaw the base transform overwrote.
                bone.setRotY(bone.getRotY() + bone.getInitialSnapshot().getRotY());
            };
        }

        return base;
    }
}
