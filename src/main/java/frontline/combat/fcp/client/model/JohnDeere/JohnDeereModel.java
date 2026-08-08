package frontline.combat.fcp.client.model.JohnDeere;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.JohnDeere.JohnDeereEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class JohnDeereModel extends FCPVehicleModel<JohnDeereEntity> {

    @Override
    public ResourceLocation getModelResource(JohnDeereEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/john_deere.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<JohnDeereEntity> collectTransform(String boneName) {
        // Tractor: small steering front wheels + large drive rear wheels.
        // Radii in blocks, read from john_deere.geo.json (px / 16).
        VehicleModel.TransformContext<JohnDeereEntity> wheels = WheelRotationTransforms.matchWheels(boneName,
                WheelRotationTransforms.steered(1.081, 30f, "WheelL0Turn", "WheelR0Turn"),
                WheelRotationTransforms.rolling(1.363, "WheelL0", "WheelR0"));
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}