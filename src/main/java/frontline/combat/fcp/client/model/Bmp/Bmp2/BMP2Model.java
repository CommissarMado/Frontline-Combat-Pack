package frontline.combat.fcp.client.model.Bmp.Bmp2;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Bmp.BmpTrackPaths;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp2.BMP2Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class BMP2Model extends FCPVehicleModel<BMP2Entity> {

    private static final String CANNON_WEAPON = "Cannon";

    @Override
    public ResourceLocation getModelResource(BMP2Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/bmp2.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }


    @Override
    public @Nullable VehicleModel.TransformContext<BMP2Entity> collectTransform(String boneName) {
        return switch (boneName) {
            case "BarrelOccilator" -> barrelRecoil(0);

            case "WheelL0", "WheelR0", "WheelL1", "WheelR1", "WheelL2", "WheelR2", "WheelL3", "WheelR3",
                 "WheelL4", "WheelR4", "WheelL5", "WheelR5", "WheelL6", "WheelR6", "WheelL7", "WheelR7" -> (bone, vehicle, state) -> {
                float wheelRot = Mth.lerp(state.getPartialTick(), vehicle.getPrevWheelRotation(), vehicle.getWheelRotation());
                bone.setRotX((float) Math.toRadians(-wheelRot));
            };
            default -> super.collectTransform(boneName);
        };
    }

    private VehicleModel.TransformContext<BMP2Entity> barrelRecoil(int barrelIndex) {
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

    @Override
    protected FCPTrackPath getTrackPath() {
        // Shared with every other BMP variant. Flip just this vehicle with .reversed(),
        // .rotationInverted() or .rotationOffset(180) - no table changes needed.
        return BmpTrackPaths.BMP.reversed();
    }
}
