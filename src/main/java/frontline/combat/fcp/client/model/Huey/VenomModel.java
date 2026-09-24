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
            // TurretYawRange. No more single hand-rolled "camera" bone. See VenomGunshipModel's
            // comment for the full "mount"/"barrelMount" geo fix - "turret"'s ancestor chain here
            // (Uh-1's [0,180,0] plus group8's own [0,-90,0]) composed to a net 90-degree Y
            // rotation, which made the engine's hardcoded local-X barrel pitch twist the sensor
            // around its own boresight instead of elevating it. A static "mount" bone (rotation
            // [0,90,0], inserted between group8 and turret, using per-cube pivot/rotation rather
            // than rewriting cube origin/size so the UVs stay intact) cancels that out; a second
            // static "barrelMount" bone (rotation [0,0,180], between turret and barrel) corrects
            // the elevation direction, which "mount"'s sign choice leaves inverted on its own.
            default -> super.collectTransform(boneName);
        };
    }
}
