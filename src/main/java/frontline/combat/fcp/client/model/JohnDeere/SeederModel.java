package frontline.combat.fcp.client.model.JohnDeere;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.JohnDeere.JohnDeereEntity;
import frontline.combat.fcp.entity.vehicle.JohnDeere.SeederEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class SeederModel extends FCPVehicleModel<SeederEntity> {

    @Override
    public ResourceLocation getModelResource(SeederEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/seeder.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<SeederEntity> collectTransform(String boneName) {
        VehicleModel.TransformContext<SeederEntity> wheels = WheelRotationTransforms.matchWheels(boneName,
                // Seeder (towed) has three wheel sizes plus press rollers; all just roll.
                // Radii in blocks from seeder.geo.json (px / 16).
                WheelRotationTransforms.rolling(0.813, "WheelL2", "WheelR2"),
                WheelRotationTransforms.rolling(0.659, "WheelL0", "WheelR0", "WheelL1", "WheelR1"),
                WheelRotationTransforms.rolling(0.432,
                        "TopWheelL0", "TopWheelR0", "TopWheelL1", "TopWheelR1",
                        "RollerL0", "RollerR0", "RollerL1", "RollerR1", "RollerL2", "RollerR2", "RollerL3", "RollerR3",
                        "RollerL4", "RollerR4", "RollerL5", "RollerR5", "RollerL6", "RollerR6"));
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
