package frontline.combat.fcp.client.model.NovatorUnarmed;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.NovatorUnarmed.NovatorUnarmedEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class NovatorUnarmedModel extends FCPVehicleModel<NovatorUnarmedEntity> {
    @Override
    public ResourceLocation getModelResource(NovatorUnarmedEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/novator_unarmed.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {return false;}

    @Override
    public @Nullable VehicleModel.TransformContext<NovatorUnarmedEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<NovatorUnarmedEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.621, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<NovatorUnarmedEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.621,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
