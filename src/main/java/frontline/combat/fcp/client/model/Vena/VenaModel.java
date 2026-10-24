package frontline.combat.fcp.client.model.Vena;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Vena.VenaEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** 2S31 Vena. The 2A80's tube ("GUN") recoils like the Msta's; no track or wheel animation. */
public class VenaModel extends VehicleModel<VenaEntity> {

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
                CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.MSTA);
            };
        }
        return super.collectTransform(boneName);
    }
}
