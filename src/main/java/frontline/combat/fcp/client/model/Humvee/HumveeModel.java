package frontline.combat.fcp.client.model.Humvee;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Btr82.BTR82Entity;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeEntity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class HumveeModel extends FCPVehicleModel<HumveeEntity> {
    @Override
    public ResourceLocation getModelResource(HumveeEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/humvee.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }


    @Override
    public @Nullable VehicleModel.TransformContext<HumveeEntity> collectTransform(String boneName) {

        VehicleModel.TransformContext<HumveeEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.6, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<HumveeEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.6,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        return super.collectTransform(boneName);
    }
}
