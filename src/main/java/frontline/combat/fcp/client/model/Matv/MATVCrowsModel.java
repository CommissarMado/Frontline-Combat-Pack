package frontline.combat.fcp.client.model.Matv;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Matv.MATVCrowsEntity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVTOWEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MATVCrowsModel extends FCPVehicleModel<MATVCrowsEntity> {

    private static final String CANNON_WEAPON = "PassengerMachineGun";

    @Override
    public ResourceLocation getModelResource(MATVCrowsEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/matv_crows.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<MATVCrowsEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<MATVCrowsEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<MATVCrowsEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }

    private VehicleModel.TransformContext<MATVCrowsEntity> barrelRecoil(int barrelIndex) {
        return (bone, vehicle, state) -> {
            ModelBoneTransforms.clearRecoilOffsets(bone);
            if (vehicle.getCannonRecoilTime() <= 0) {
                return;
            }
            if (!CANNON_WEAPON.equals(vehicle.getGunName(1))) {
                return;
            }
            CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.SIDETOSIDE);
        };
    }
}