package frontline.combat.fcp.client.model.T72av;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Type10.Type10TrackPaths;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp1.BMP1AMEntity;
import frontline.combat.fcp.entity.vehicle.T72av.T72AVEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class T72AVModel extends FCPVehicleModel<T72AVEntity> {

    private static final String CANNON_WEAPON = "Cannon";

    @Override
    public ResourceLocation getModelResource(T72AVEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/t72av.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<T72AVEntity> collectTransform(String boneName) {
        if ("Barrel".equals(boneName)) {
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
        return T72avTrackPaths.T72AV.reversed();
    }
}
