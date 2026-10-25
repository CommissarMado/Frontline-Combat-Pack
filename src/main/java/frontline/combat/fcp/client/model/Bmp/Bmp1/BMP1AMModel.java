package frontline.combat.fcp.client.model.Bmp.Bmp1;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Bmp.BmpTrackPaths;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp1.BMP1AMEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class BMP1AMModel extends FCPVehicleModel<BMP1AMEntity> {

    private static final String CANNON_WEAPON = "Cannon";

    @Override
    public ResourceLocation getModelResource(BMP1AMEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/bmp1_am.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<BMP1AMEntity> collectTransform(String boneName) {
        if ("GUN3".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float bindRotX = bone.getInitialSnapshot().getRotX();

                ModelBoneTransforms.clearRecoilOffsets(bone);
                if (vehicle.getCannonRecoilTime() > 0 && CANNON_WEAPON.equals(CannonRecoilTransforms.gunnerGunName(vehicle))) {
                    CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.FORWARDBACK);
                }

                bone.setRotX(bindRotX + bone.getRotX());
            };
        }

        return super.collectTransform(boneName);
    }

    @Override
    protected FCPTrackPath getTrackPath() {
        // Shared with every other BMP variant. Flip just this vehicle with .reversed(),
        // .rotationInverted() or .rotationOffset(180) - no table changes needed.
        return BmpTrackPaths.BMP.reversed();
    }
}
