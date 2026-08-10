package frontline.combat.fcp.client.model.Stryker;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerDragoonEntity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerM2Entity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerMGSEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class StrykerMGSModel extends FCPVehicleModel<StrykerMGSEntity> {

    private static final String CANNON_WEAPON = "Cannon";

    @Override
    public ResourceLocation getModelResource(StrykerMGSEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/stryker_mgs.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<StrykerMGSEntity> collectTransform(String boneName) {

        VehicleModel.TransformContext<StrykerMGSEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.534, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<StrykerMGSEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.534,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        if ("BarrelOccilator".equals(boneName)) {
            return barrelRecoil(0);
        }

        return super.collectTransform(boneName);
    }

    private VehicleModel.TransformContext<StrykerMGSEntity> barrelRecoil(int barrelIndex) {
        return (bone, vehicle, state) -> {
            ModelBoneTransforms.clearRecoilOffsets(bone);
            if (vehicle.getCannonRecoilTime() <= 0) {
                return;
            }
            if (!CANNON_WEAPON.equals(CannonRecoilTransforms.gunnerGunName(vehicle))) {
                return;
            }
            CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.STANDARD);
        };
    }
}
