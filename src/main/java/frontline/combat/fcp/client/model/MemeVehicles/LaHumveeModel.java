package frontline.combat.fcp.client.model.MemeVehicles;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.MemeVehicles.BigBirdEntity;
import frontline.combat.fcp.entity.vehicle.MemeVehicles.LaHumveeEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class LaHumveeModel extends VehicleModel<LaHumveeEntity> {

    @Override
    public ResourceLocation getModelResource(LaHumveeEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/humvee_mk19.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<LaHumveeEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "turret" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "extrawheel" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            default -> super.collectTransform(boneName);
        };
    }
}
