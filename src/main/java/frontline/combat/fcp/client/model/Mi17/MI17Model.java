package frontline.combat.fcp.client.model.Mi17;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Mi17.MI17Entity;
import frontline.combat.fcp.entity.vehicle.Viper.ViperEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class MI17Model extends VehicleModel<MI17Entity> {

    @Override
    public ResourceLocation getModelResource(MI17Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/mi17.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<MI17Entity> collectTransform(String boneName) {
        return switch (boneName) {
            case "propeller" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "tailPropeller" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            default -> super.collectTransform(boneName);
        };
    }
}
