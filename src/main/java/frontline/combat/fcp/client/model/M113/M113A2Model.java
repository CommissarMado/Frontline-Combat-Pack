package frontline.combat.fcp.client.model.M113;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.entity.vehicle.M113.M113Entity;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animation.AnimationState;

/**
 * M113A2 (geo/m113a2.geo.json). The open M2 traverses on "turret" and elevates on "barrel"; the stowage
 * ("toggle1") shows only while fitted.
 */
public class M113A2Model extends FCPVehicleModel<M113Entity> {

    private static final ResourceLocation MODEL = new ResourceLocation(FCP.MODID, "geo/m113a2.geo.json");

    private static final FCPTrackPath TRACK_PATH = M113A2TrackPaths.M113A2.reversed();

    @Override
    protected FCPTrackPath getTrackPath() {
        return TRACK_PATH;
    }

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
