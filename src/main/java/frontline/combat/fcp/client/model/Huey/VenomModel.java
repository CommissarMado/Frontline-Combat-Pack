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
            // "turret"/"barrel" (the front sensor) are intentionally NOT handled here, same as
            // VenomGunshipModel/VenomDoorGunsModel - the base VehicleModel drives them natively
            // from venom.json's TurretPos/BarrelPos/TurretControllerIndex/TurretPitchRange/
            // TurretYawRange. No more single hand-rolled "camera" bone (which also had pitch on
            // the wrong axis - Z instead of the engine's hardcoded local X for "barrel").
            default -> super.collectTransform(boneName);
        };
    }
}
