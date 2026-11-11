package frontline.combat.fcp.client.model.Matv;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Matv.MATV9In1Entity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MATV9In1Model extends FCPVehicleModel<MATV9In1Entity> {

    private static final String CANNON_WEAPON = "PassengerMachineGun";

    @Override
    public ResourceLocation getModelResource(MATV9In1Entity animatable) {
        // "matv_9in1" or "matv_mk19" (same entity class): the Mk 19 variant's geo is the 9-in-1's without the other two turrets.
        boolean mk19 = net.minecraft.world.entity.EntityType.getKey(animatable.getType()).getPath().equals("matv_mk19");
        return new ResourceLocation(FCP.MODID, mk19 ? "geo/matv_mk19.geo.json" : "geo/matv9in1.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<MATV9In1Entity> collectTransform(String boneName) {
        VehicleModel.TransformContext<MATV9In1Entity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.631, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<MATV9In1Entity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.631,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }

    private VehicleModel.TransformContext<MATV9In1Entity> barrelRecoil(int barrelIndex) {
        return (bone, vehicle, state) -> {
            ModelBoneTransforms.clearRecoilOffsets(bone);
            if (vehicle.getCannonRecoilTime() <= 0) {
                return;
            }
            if (!CANNON_WEAPON.equals(CannonRecoilTransforms.gunnerGunName(vehicle))) {
                return;
            }
            CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.SIDETOSIDE);
        };
    }
}
