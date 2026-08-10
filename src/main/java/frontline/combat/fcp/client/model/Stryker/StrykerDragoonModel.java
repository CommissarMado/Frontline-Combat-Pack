package frontline.combat.fcp.client.model.Stryker;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.M109.M109Entity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerDragoonEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class StrykerDragoonModel extends FCPVehicleModel<StrykerDragoonEntity> {

    private static final String CANNON_WEAPON = "Cannon";

    @Override public ResourceLocation getModelResource(StrykerDragoonEntity a) { return new ResourceLocation(FCP.MODID, "geo/stryker_dragoon.geo.json"); }
    @Override public boolean hideForTurretControllerWhileZooming() { return false; }

    @Override public @Nullable VehicleModel.TransformContext<StrykerDragoonEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<StrykerDragoonEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.534, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;
        VehicleModel.TransformContext<StrykerDragoonEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.534,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        if ("BarrelOccilator".equals(boneName)) {
            return barrelRecoil(0);
        }

        return super.collectTransform(boneName);
    }

    private VehicleModel.TransformContext<StrykerDragoonEntity> barrelRecoil(int barrelIndex) {
        return (bone, vehicle, state) -> {
            ModelBoneTransforms.clearRecoilOffsets(bone);
            if (vehicle.getCannonRecoilTime() <= 0) {
                return;
            }
            if (!CANNON_WEAPON.equals(CannonRecoilTransforms.gunnerGunName(vehicle))) {
                return;
            }
            CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.LIGHT);
        };
    }
}
