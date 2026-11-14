package frontline.combat.fcp.client.model.Dingo;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeUnarmedEntity;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;

/**
 * ATF Dingo (dingo_mg3, dingo_m2, dingo_gmg): one model on the shared attachment entity, like the Caiman. The geo is
 * picked from the registry name (each keeps one gun under "barrel"); "toggle1" (roof box) and "toggle2" (front) are
 * removable hull attachments.
 */
public class DingoModel extends VehicleModel<HumveeUnarmedEntity> {

    /** Tyre radius in blocks, from the wheel bones. */
    private static final double WHEEL_RADIUS = 0.584;

    @Override
    public ResourceLocation getModelResource(HumveeUnarmedEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(FCP.MODID, "geo/" + animatable.humveeName() + ".geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<HumveeUnarmedEntity> collectTransform(String boneName) {
        // Front axle (wheel3 right, wheel5 left) steers and rolls; the rear one only rolls.
        VehicleModel.TransformContext<HumveeUnarmedEntity> front =
                WheelRotationTransforms.matchAnyTurn(boneName, WHEEL_RADIUS, 30f, "wheel3", "wheel5");
        if (front != null) return front;
        VehicleModel.TransformContext<HumveeUnarmedEntity> rear =
                WheelRotationTransforms.matchAny(boneName, WHEEL_RADIUS, "wheel2", "wheel4");
        if (rear != null) return rear;

        return super.collectTransform(boneName);
    }

    @Override
    public void setCustomAnimations(HumveeUnarmedEntity vehicle, long instanceId, AnimationState<HumveeUnarmedEntity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        HumveeAttachments.applyVisibility(this, vehicle);
    }
}
