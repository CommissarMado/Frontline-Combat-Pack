package frontline.combat.fcp.client.model.Bmp3;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Bmp3.Bmp3Entity;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * BMP-3 family. Only the 30 mm recoils ("GUN2" on the BMP-3s, "GUN3" on the BRM-3K); the 100 mm,
 * which also fires the 9M117 as its second ammo type, stays put. The bow PKTs
 * ("pkt", "pkt2") follow their seats' aim. No track or wheel animation: none of the running gear bones
 * match SBW's wheel/track names.
 */
public class Bmp3Model extends VehicleModel<Bmp3Entity> {

    @Override
    public ResourceLocation getModelResource(Bmp3Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/" + HumveeAttachments.vehicleName(animatable.getType()) + ".geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<Bmp3Entity> collectTransform(String boneName) {
        switch (boneName) {
            case "GUN2":
            case "GUN3":
                // Only the 30 mm recoils (BMP-3s: "GUN2"; BRM-3K: "GUN3").
                return (bone, vehicle, state) -> {
                    ModelBoneTransforms.clearRecoilOffsets(bone);
                    if (vehicle.getCannonRecoilTime() <= 0) return;
                    if (!"Autocannon".equals(CannonRecoilTransforms.gunnerGunName(vehicle))) return;
                    CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.LIGHT);
                };
            case "pkt":
                return (bone, v, s) -> {
                    bone.setRotY(v.pktYaw(Bmp3Entity.PKT_SEAT, s.getPartialTick()) * Mth.DEG_TO_RAD);
                    bone.setRotX(v.pktPitch(Bmp3Entity.PKT_SEAT, s.getPartialTick()) * Mth.DEG_TO_RAD);
                };
            case "pkt2":
                return (bone, v, s) -> {
                    bone.setRotY(v.pktYaw(Bmp3Entity.PKT2_SEAT, s.getPartialTick()) * Mth.DEG_TO_RAD);
                    bone.setRotX(v.pktPitch(Bmp3Entity.PKT2_SEAT, s.getPartialTick()) * Mth.DEG_TO_RAD);
                };
            default:
                return super.collectTransform(boneName);
        }
    }
}
