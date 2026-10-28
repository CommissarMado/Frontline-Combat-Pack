package frontline.combat.fcp.client.model.Lav;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Huey.HueyEntity;
import frontline.combat.fcp.entity.vehicle.Lav.Lav25Entity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerM2Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public class Lav25Model extends FCPVehicleModel<Lav25Entity> {

    private static final String CANNON_WEAPON = "Cannon";

    @Override
    public ResourceLocation getModelResource(Lav25Entity animatable) {
        // lav25_mg is the LAV-25 with the commander's pintle M240; same skins.
        return new ResourceLocation(FCP.MODID, "geo/" + EntityType.getKey(animatable.getType()).getPath() + ".geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<Lav25Entity> collectTransform(String boneName) {

        if ("BarrelOccilator".equals(boneName)) {
            return barrelRecoil(0);
        }

        // lav25_mg: the pintle M240 is one bone, so SBW's weapon-station yaw and pitch both turn it.
        if ("weaponstationbarrel".equals(boneName)) {
            VehicleModel.TransformContext<Lav25Entity> yaw = super.collectTransform("passengerWeaponStationYaw");
            VehicleModel.TransformContext<Lav25Entity> pitch = super.collectTransform("passengerWeaponStationPitch");
            return (bone, vehicle, state) -> {
                if (yaw != null) yaw.transform(bone, vehicle, state);
                if (pitch != null) pitch.transform(bone, vehicle, state);
            };
        }

        // Steering wheels (roll on X + pivot on Y). Lav25Entity implements
        // SteerableVehicle, so these pivot from its steering angle.
        VehicleModel.TransformContext<Lav25Entity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.534, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        // Plain rolling wheels.
        VehicleModel.TransformContext<Lav25Entity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.534,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
    private VehicleModel.TransformContext<Lav25Entity> barrelRecoil(int barrelIndex) {
        return (bone, vehicle, state) -> {
            ModelBoneTransforms.clearRecoilOffsets(bone);
            if (vehicle.getCannonRecoilTime() <= 0) {
                return;
            }
            if (!CANNON_WEAPON.equals(CannonRecoilTransforms.gunnerGunName(vehicle))) {
                return;
            }
            CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.FORWARDBACK);
        };
    }
}