package frontline.combat.fcp.client.model.Stryker;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerTowEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class StrykerTowModel extends FCPVehicleModel<StrykerTowEntity> {

    @Override public ResourceLocation getModelResource(StrykerTowEntity a) { return new ResourceLocation(FCP.MODID, "geo/stryker_tow.geo.json"); }
    @Override public boolean hideForTurretControllerWhileZooming() { return false; }

    @Override public @Nullable VehicleModel.TransformContext<StrykerTowEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<StrykerTowEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6,
                        "wheRR", "wheRR2", "wheRR3", "wheRR4", "wheRR5", "wheRR6", "wheRR7", "wheRR8");
        if (wheels != null) return wheels;

        // Missiles disappear as they're fired (Viper-style). Magazine is 2: missile1 hides
        // once one is gone (ammo <= 1), missile2 once both are gone (ammo <= 0).
        if ("missile1".equals(boneName))
            return (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("TOW", 1));
        if ("missile2".equals(boneName))
            return (bone, vehicle, state) -> bone.setHidden(vehicle.GetWeaponState("TOW", 0));

        return super.collectTransform(boneName);
    }
}
