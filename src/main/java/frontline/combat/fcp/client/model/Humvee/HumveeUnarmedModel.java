package frontline.combat.fcp.client.model.Humvee;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeUnarmedEntity;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animation.AnimationState;
import org.jetbrains.annotations.Nullable;

/**
 * One model shared by every unarmed HMMWV variant. The geo is chosen from the entity's
 * registry name, and the "Attachments" tree visibility is applied per frame after the base
 * transforms run.
 */
public class HumveeUnarmedModel extends VehicleModel<HumveeUnarmedEntity> {

    @Override
    public ResourceLocation getModelResource(HumveeUnarmedEntity animatable) {
        String name = animatable.humveeName();
        // hmmwv_asrad ships as "<name>.json"; all others as "<name>.geo.json".
        String path = "hmmwv_asrad".equals(name) ? "geo/" + name + ".json" : "geo/" + name + ".geo.json";
        return new ResourceLocation(FCP.MODID, path);
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<HumveeUnarmedEntity> collectTransform(String boneName) {
        // Front wheels steer (pivot on Y) and roll; rear wheels only roll.
        VehicleModel.TransformContext<HumveeUnarmedEntity> front =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f, "whell1", "whell2");
        if (front != null) return front;
        VehicleModel.TransformContext<HumveeUnarmedEntity> rear =
                WheelRotationTransforms.matchAny(boneName, 0.6, "whell3", "whell4");
        if (rear != null) return rear;

        return super.collectTransform(boneName);
    }

    @Override
    public void setCustomAnimations(HumveeUnarmedEntity vehicle, long instanceId, AnimationState<HumveeUnarmedEntity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        HumveeAttachments.applyVisibility(this, vehicle);
    }
}
