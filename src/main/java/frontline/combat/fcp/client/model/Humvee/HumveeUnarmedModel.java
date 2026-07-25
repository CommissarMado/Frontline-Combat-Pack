package frontline.combat.fcp.client.model.Humvee;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeUnarmedEntity;
import frontline.combat.fcp.vehicle.humvee.HumveeAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
        VehicleModel.TransformContext<HumveeUnarmedEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6, "whell1", "whell2", "whell3", "whell4");
        if (wheels != null) return wheels;

        if ("barrel".equals(boneName)) {
            // The TOW launcher barrels are modelled pointing the opposite way (+Z) to the
            // other stations (-Z), so SuperbWarfare's pitch comes out inverted on them.
            // Flip the pitch sign for the TOW variants; everything else keeps the vanilla
            // behaviour (sign -1, identical to the base handler).
            return (bone, vehicle, animationState) -> {
                float xRot = Mth.lerp(animationState.getPartialTick(),
                        vehicle.getTurretXRotO(), vehicle.getTurretXRot());
                float sign = vehicle.humveeName().contains("tow") ? 1f : -1f;
                bone.setRotX(Mth.clamp(sign * xRot,
                        vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);
            };
        }

        return super.collectTransform(boneName);
    }

    @Override
    public void setCustomAnimations(HumveeUnarmedEntity vehicle, long instanceId, AnimationState<HumveeUnarmedEntity> animationState) {
        super.setCustomAnimations(vehicle, instanceId, animationState);
        HumveeAttachments.applyVisibility(this, vehicle);
    }
}
