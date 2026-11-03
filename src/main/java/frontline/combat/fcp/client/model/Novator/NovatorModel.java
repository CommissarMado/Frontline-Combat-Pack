package frontline.combat.fcp.client.model.Novator;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Huey.HueyEntity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVEntity;
import frontline.combat.fcp.entity.vehicle.Novator.NovatorEntity;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class NovatorModel extends FCPVehicleModel<NovatorEntity> {
    @Override
    public ResourceLocation getModelResource(NovatorEntity animatable) {
        // "novator" or "novator_stugna" (same entity class, registry name picks the geo).
        return new ResourceLocation(FCP.MODID, "geo/" + net.minecraft.world.entity.EntityType.getKey(animatable.getType()).getPath() + ".geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }


    @Override
    public @Nullable VehicleModel.TransformContext<NovatorEntity> collectTransform(String boneName) {

        VehicleModel.TransformContext<NovatorEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.621, 30f,
                        "WheelL0Turn", "WheelR0Turn", "WheelL1Turn", "WheelR1Turn");
        if (turn != null) return turn;

        VehicleModel.TransformContext<NovatorEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.621,
                        "WheelL0", "WheelR0", "WheelL1", "WheelR1");
        if (wheels != null) return wheels;

        // Novator Stugna: the spent Stugna-P container comes off halfway through the reload (LauncherTube).
        if ("missile".equals(boneName)) {
            return (bone, vehicle, state) -> bone.setHidden(vehicle.launcherTubeOff());
        }

        return super.collectTransform(boneName);
    }
}
