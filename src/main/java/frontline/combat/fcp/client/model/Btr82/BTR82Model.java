package frontline.combat.fcp.client.model.Btr82;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Btr82.BTR82Entity;
import frontline.combat.fcp.entity.vehicle.Lav.Lav25Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class BTR82Model extends FCPVehicleModel<BTR82Entity> {

    private static final String CANNON_WEAPON = "Cannon";

    @Override
    public ResourceLocation getModelResource(BTR82Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/btr82.geo.json");
    }
    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }


    @Override
    public @Nullable VehicleModel.TransformContext<BTR82Entity> collectTransform(String boneName) {
        return switch (boneName) {

            case "BarrelOccilator" -> barrelRecoil(0);

            case "WheelTurnL1", "WheelTurnR1", "WheelTurnL2", "WheelTurnR2" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());
                bone.setRotX((float) Math.toRadians(-wheelRot));

                float steeringAngle = Mth.lerp(state.getPartialTick(), vehicle.getPrevSteeringAngle(), vehicle.getSteeringAngle());
                steeringAngle = Mth.clamp(steeringAngle, -30f, 30f);
                bone.setRotY((float) Math.toRadians(steeringAngle));
            };

            case "WheelL3", "WheelR3", "WheelL4", "WheelR4" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());
                bone.setRotX((float) Math.toRadians(-wheelRot));
            };
            default -> super.collectTransform(boneName);
        };
    }

    private VehicleModel.TransformContext<BTR82Entity> barrelRecoil(int barrelIndex) {
        return (bone, vehicle, state) -> {
            ModelBoneTransforms.clearRecoilOffsets(bone);
            if (vehicle.getCannonRecoilTime() <= 0) {
                return;
            }
            if (!CANNON_WEAPON.equals(vehicle.getGunName(1))) {
                return;
            }
            CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.FORWARDBACK);
        };
    }
}
