package frontline.combat.fcp.client.model.M113;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.entity.vehicle.M113.M113Entity;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animation.AnimationState;

/**
 * Skif (geo/skif.geo.json).
 */
public class SkifModel extends FCPVehicleModel<M113Entity> {

    private static final FCPTrackPath TRACK_PATH = SkifTrackPaths.SKIF.reversed();

    @Override
    protected FCPTrackPath getTrackPath() {
        return TRACK_PATH;
    }

    private static final ResourceLocation MODEL = new ResourceLocation(FCP.MODID, "geo/skif.geo.json");

    @Override
    public ResourceLocation getModelResource(M113Entity animatable) {
        return MODEL;
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public void setCustomAnimations(M113Entity vehicle, long instanceId, AnimationState<M113Entity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        HumveeAttachments.applyVisibility(this, vehicle);
    }
}
