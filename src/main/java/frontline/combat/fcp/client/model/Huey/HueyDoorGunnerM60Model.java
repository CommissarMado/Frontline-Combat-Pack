package frontline.combat.fcp.client.model.Huey;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Huey.HueyDoorGunnerM60Entity;
import frontline.combat.fcp.entity.vehicle.Huey.HueyEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class HueyDoorGunnerM60Model extends VehicleModel<HueyDoorGunnerM60Entity> {

    @Override
    public ResourceLocation getModelResource(HueyDoorGunnerM60Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/huey_m60.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<HueyDoorGunnerM60Entity> collectTransform(String boneName) {
        return switch (boneName) {
            case "propeller" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "tailPropeller" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            default -> super.collectTransform(boneName);
        };
    }
}
