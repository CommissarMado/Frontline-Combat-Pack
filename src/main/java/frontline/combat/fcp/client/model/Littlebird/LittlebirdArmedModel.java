package frontline.combat.fcp.client.model.Littlebird;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Huey.HueyEntity;
import frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdArmedEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class LittlebirdArmedModel extends VehicleModel<LittlebirdArmedEntity> {

    @Override
    public ResourceLocation getModelResource(LittlebirdArmedEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/littlebird_armed.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<LittlebirdArmedEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "propeller" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "tailPropeller" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "BarrelRotationController1", "BarrelRotationController2" ->
                    (bone, vehicle, state) -> bone.setRotZ(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRot0(), vehicle.getBarrelRot()));
            default -> super.collectTransform(boneName);
        };
    }
}
