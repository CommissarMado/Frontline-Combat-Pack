package frontline.combat.fcp.client.model.M113;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.entity.vehicle.M113.M113Entity;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animation.AnimationState;

/**
 * M113, M113A2 and Skif. No track or wheel animation (user). The M113A2's open M2 traverses on "turret" and elevates
 * on "barrel"; its stowage ("toggle1") shows only while fitted.
 */
public class M113Model extends FCPVehicleModel<M113Entity> {

    @Override
    public ResourceLocation getModelResource(M113Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/" + animatable.variant() + ".geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<M113Entity> collectTransform(String boneName) {
        // Road wheels and track links stay as modelled.
        if (boneName.startsWith("wheel") || boneName.startsWith("GUS")) return null;
        return super.collectTransform(boneName);
    }

    @Override
    public void setCustomAnimations(M113Entity vehicle, long instanceId, AnimationState<M113Entity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        HumveeAttachments.applyVisibility(this, vehicle);
    }
}
