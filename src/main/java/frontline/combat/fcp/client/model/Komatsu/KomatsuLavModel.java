package frontline.combat.fcp.client.model.Komatsu;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Komatsu.KomatsuLavEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * Komatsu LAV (all three). Built nose +Z in Blockbench: the hull root ("group2") is turned 180 in the geo, so the
 * wheels under it roll the other way round their X axis - they use the non-inverted roll; steering (Y) is unaffected.
 * "wheel"/"wheel2" are the front (steering) pair, "wheel5"/"wheel6" the rear. The open M2 traverses on "turret" and
 * elevates on "barrel" (baked plain bones, KomatsuGeo).
 */
public class KomatsuLavModel extends FCPVehicleModel<KomatsuLavEntity> {

    /** Tyre radius in blocks (tyre 18.75 px across; pivots centred on the axles by scratchpad WheelCentre). */
    private static final double WHEEL_RADIUS = 0.586;

    @Override
    public ResourceLocation getModelResource(KomatsuLavEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/" + EntityType.getKey(animatable.getType()).getPath() + ".geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<KomatsuLavEntity> collectTransform(String boneName) {
        if (boneName.equals("wheel") || boneName.equals("wheel2")) {
            VehicleModel.TransformContext<KomatsuLavEntity> steer = WheelRotationTransforms.matchTurn(boneName, boneName, WHEEL_RADIUS, 30f);
            VehicleModel.TransformContext<KomatsuLavEntity> roll = WheelRotationTransforms.match(boneName, boneName, WHEEL_RADIUS,
                    WheelRotationTransforms.SpinAxis.X, false);
            return (bone, vehicle, state) -> {
                steer.transform(bone, vehicle, state);
                roll.transform(bone, vehicle, state);
            };
        }
        if (boneName.equals("wheel5") || boneName.equals("wheel6")) {
            return WheelRotationTransforms.match(boneName, boneName, WHEEL_RADIUS, WheelRotationTransforms.SpinAxis.X, false);
        }
        return super.collectTransform(boneName);
    }
}
