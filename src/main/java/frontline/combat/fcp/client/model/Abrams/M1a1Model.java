package frontline.combat.fcp.client.model.Abrams;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Abrams.M1a1Entity;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animation.AnimationState;


/**
 * One model for every Abrams variant: the geo is picked from the registry name, all share the
 * M1A1 hull's track path, and the "toggle" bones are attachments (see HumveeAttachments).
 * <p>
 * The loader's MG ("turret3" yaw, "barrel3" pitch) is a third turret SBW has no slot for: SBW's
 * BoundBones only work in its GeoVehicleRenderer, not the GeckoLib VehicleRenderer FCP vehicles use.
 * So the model aims it at the loader's look, clamped to the loader seat's own yaw/pitch limits from
 * the vehicle JSON - the same limits SBW applies to the loader's view - so gun and view agree.
 */
public class M1a1Model extends FCPVehicleModel<M1a1Entity> {

    /** The M1A1 hull's track path at the Abrams geos' 0.8x size. */
    private static final FCPTrackPath TRACK_PATH = M1a1TrackPaths.M1A1.reversed().scaled(0.8f);

    @Override
    public ResourceLocation getModelResource(M1a1Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/" + animatable.humveeName() + ".geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    protected FCPTrackPath getTrackPath() {
        return TRACK_PATH;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<M1a1Entity> collectTransform(String boneName) {
        // Same sign conventions as SBW's passenger weapon station: yaw bone = -(look offset),
        // pitch bone = -(look pitch), both relative to the turret the MG is mounted on.
        if (boneName.equals("turret3")) {
            return (bone, vehicle, state) -> bone.setRotY(-vehicle.loaderAim(state.getPartialTick())[0] * Mth.DEG_TO_RAD);
        }
        if (boneName.equals("barrel3")) {
            return (bone, vehicle, state) -> bone.setRotX(-vehicle.loaderAim(state.getPartialTick())[1] * Mth.DEG_TO_RAD);
        }
        // Main gun tube recoils straight back - only for the cannon, not the coax or gun-top M2.
        if (boneName.equals("STVOL")) {
            return (bone, vehicle, state) -> {
                ModelBoneTransforms.clearRecoilOffsets(bone);
                if (vehicle.getCannonRecoilTime() <= 0) return;
                if (!"Cannon".equals(CannonRecoilTransforms.gunnerGunName(vehicle))) return;
                CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.ABRAMS);
            };
        }
        return super.collectTransform(boneName);
    }

    @Override
    public void setCustomAnimations(M1a1Entity vehicle, long instanceId, AnimationState<M1a1Entity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        HumveeAttachments.applyVisibility(this, vehicle);
    }
}
