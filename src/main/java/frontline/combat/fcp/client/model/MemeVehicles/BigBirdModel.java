package frontline.combat.fcp.client.model.MemeVehicles;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdEntity;
import frontline.combat.fcp.entity.vehicle.MemeVehicles.BigBirdEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class BigBirdModel extends VehicleModel<BigBirdEntity> {

    @Override
    public ResourceLocation getModelResource(BigBirdEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/bigbird.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<BigBirdEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "propeller" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "tailPropeller" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            default -> super.collectTransform(boneName);
        };
    }
}
