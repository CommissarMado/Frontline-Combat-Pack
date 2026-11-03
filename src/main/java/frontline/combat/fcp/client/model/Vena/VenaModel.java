package frontline.combat.fcp.client.model.Vena;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Bmp3.Bmp3TrackPaths;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Vena.VenaEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** 2S31 Vena. The 2A80's tube ("GUN") recoils back like the Msta's; tracks share the BMP-3 hull's path. */
public class VenaModel extends FCPVehicleModel<VenaEntity> {

    private static final FCPTrackPath TRACK_PATH = Bmp3TrackPaths.BMP3.reversed();

    @Override
    protected FCPTrackPath getTrackPath() {
        return TRACK_PATH;
    }

    @Override
    public ResourceLocation getModelResource(VenaEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/2s31.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<VenaEntity> collectTransform(String boneName) {
        if ("GUN".equals(boneName)) {
            return (bone, vehicle, state) -> {
                ModelBoneTransforms.clearRecoilOffsets(bone);
                if (vehicle.getCannonRecoilTime() <= 0) return;
                if (!"Main".equals(CannonRecoilTransforms.gunnerGunName(vehicle))) return;
                CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.VENA);
            };
        }
        return super.collectTransform(boneName);
    }
}
