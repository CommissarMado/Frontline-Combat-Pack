package frontline.combat.fcp.client.model.Btr82at;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Btr82at.BTR82ATEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Unlike every other vehicle geo in the pack, the BTR-82AT turret carries a bind-pose
 * rotation: "turret" is declared with "rotation": [0, 180, 0], and its whole subtree is
 * authored inside that flipped frame (which is why Ligths/gun1 have no [-180,0,180] of
 * their own, while group2 does - it cancels the parent flip). Two consequences:
 *
 * 1. SuperbWarfare's turret handler assigns rotY absolutely, which would wipe the 180
 *    bind and leave the turret facing backwards. The base transform runs first and the
 *    bind rotation is composed back on top of it.
 *
 * 2. Inside a 180 degree yaw the local X and Z axes are reversed, so gun elevation and
 *    recoil come out backwards. Both are mirrored after the shared logic computes them.
 *
 * Recoil lives on "oscillator" (turret -> barrel -> gun1 -> oscillator), which holds the
 * recoiling gun section only, mirroring how the base BTR-82 drives "BarrelOccilator".
 * "barrel" therefore carries elevation alone.
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
        if ("oscillator".equals(boneName)) {
            return barrelRecoil();
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
        if (base == null) return null;

        if ("turret".equals(boneName)) {
            return (bone, vehicle, state) -> {
                base.transform(bone, vehicle, state);
                // Re-apply the bind-pose yaw the base transform overwrote.
                bone.setRotY(bone.getRotY() + bone.getInitialSnapshot().getRotY());
            };
        }

        if ("barrel".equals(boneName)) {
            return (bone, vehicle, state) -> {
                base.transform(bone, vehicle, state);
                // Mirror elevation into the flipped turret frame.
                bone.setRotX(-bone.getRotX());
            };
        }

        return base;
    }

    private VehicleModel.TransformContext<BTR82ATEntity> barrelRecoil() {
        return (bone, vehicle, state) -> {
            ModelBoneTransforms.clearRecoilOffsets(bone);
            if (vehicle.getCannonRecoilTime() <= 0) {
                return;
            }
            if (!CANNON_WEAPON.equals(vehicle.getGunName(1))) {
                return;
            }
            CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.FORWARDBACK);
            // Mirror the slide and kick into the flipped turret frame so the gun
            // recoils rearward rather than out of the mantlet.
            bone.setPosZ(-bone.getPosZ());
            bone.setRotX(-bone.getRotX());
        };
    }
}
