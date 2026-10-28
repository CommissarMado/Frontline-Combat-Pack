package frontline.combat.fcp.client.model.T90M;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.T90M.T90MEntity;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animation.AnimationState;

/**
 * All four T-90Ms. No track or wheel animation. The main gun recoils on "GUN" like the Abrams; the commander's
 * RWS traverses on "weaponstationturret" and elevates on "weaponstationbarrel". The cope cage ("managal", 2022 and
 * 2024 only) shows only while fitted.
 */
public class T90MModel extends FCPVehicleModel<T90MEntity> {

    @Override
    public ResourceLocation getModelResource(T90MEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/" + animatable.variant() + ".geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<T90MEntity> collectTransform(String boneName) {
        if (boneName.startsWith("WHELL") || boneName.matches("wh\\d+")) return null;
        return switch (boneName) {
            case "GUN" -> (bone, vehicle, state) -> {
                ModelBoneTransforms.clearRecoilOffsets(bone);
                if (vehicle.getCannonRecoilTime() <= 0) return;
                if (!"Cannon".equals(CannonRecoilTransforms.gunnerGunName(vehicle))) return;
                CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.ABRAMS);
            };
            case "weaponstationturret" -> super.collectTransform("passengerWeaponStationYaw");
            case "weaponstationbarrel" -> super.collectTransform("passengerWeaponStationPitch");
            default -> super.collectTransform(boneName);
        };
    }

    @Override
    public void setCustomAnimations(T90MEntity vehicle, long instanceId, AnimationState<T90MEntity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        HumveeAttachments.applyVisibility(this, vehicle);
    }
}
