package frontline.combat.fcp.client.model.Bmp.Bmp1;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Bmp.BmpTrackPaths;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp1.BMP1UEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class BMP1UModel extends FCPVehicleModel<BMP1UEntity> {

    private static final String CANNON_WEAPON = "Cannon";

    @Override
    public ResourceLocation getModelResource(BMP1UEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/bmp1u.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }


    @Override
    public @Nullable VehicleModel.TransformContext<BMP1UEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "BarrelOccilator" -> barrelRecoil(0);

            default -> super.collectTransform(boneName);
        };
    }

    private VehicleModel.TransformContext<BMP1UEntity> barrelRecoil(int barrelIndex) {
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

    @Override
    protected FCPTrackPath getTrackPath() {
        // Shared with every other BMP variant. Flip just this vehicle with .reversed(),
        // .rotationInverted() or .rotationOffset(180) - no table changes needed.
        return BmpTrackPaths.BMP.reversed();
    }
}
