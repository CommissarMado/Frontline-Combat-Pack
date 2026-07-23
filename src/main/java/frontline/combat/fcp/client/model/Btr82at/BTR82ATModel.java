package frontline.combat.fcp.client.model.Btr82at;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Btr82at.BTR82ATEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

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
        // The AT gun has no separate BarrelOccilator bone, so elevation and recoil
        // are applied together on "barrel" (same clamp + FORWARDBACK profile as the base).
        VehicleModel.TransformContext<BTR82ATEntity> barrel =
                CannonRecoilTransforms.matchBarrelForWeapon(boneName,
                        CannonRecoilTransforms.Profile.FORWARDBACK, CANNON_WEAPON);
        if (barrel != null) return barrel;

        VehicleModel.TransformContext<BTR82ATEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<BTR82ATEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }

}
