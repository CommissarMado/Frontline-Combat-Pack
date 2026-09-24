package frontline.combat.fcp.client.model.Huey;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrGLEntity;
import frontline.combat.fcp.entity.vehicle.Huey.VenomEntity;
import frontline.combat.fcp.entity.vehicle.Viper.ViperEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class VenomModel extends VehicleModel<VenomEntity> {

    @Override
    public ResourceLocation getModelResource(VenomEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/venom.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<VenomEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "propeller" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "tailPropeller" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // Front camera bone, same as VenomGunshipModel/VenomDoorGunsModel - see
            // VenomEntity's comment: venom.geo.json has no "camera" bone yet, so this case never
            // actually matches anything, but it's here so the mesh just starts animating the day
            // a camera bone gets added to this geo file too, with no code changes needed.
            case "camera" -> (bone, vehicle, state) -> {
                bone.setRotY(vehicle.getCameraYawDeg(state.getPartialTick()) * Mth.DEG_TO_RAD);
                bone.setRotZ(vehicle.getCameraPitchDeg(state.getPartialTick()) * Mth.DEG_TO_RAD);
            };
            default -> super.collectTransform(boneName);
        };
    }
}
