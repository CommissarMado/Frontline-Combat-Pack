package frontline.combat.fcp.client.model.Himars;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Himars.HimarsEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * M142 HIMARS. The launcher pod traverses on "turret" and elevates on "barrel" (SBW's own transforms). The six
 * missiles ("group".."group6") disappear as they're fired: shots follow the ammo count (ShootPos
 * BoundUpWithAmmoAmount), so tube i goes once {@code i} or fewer remain. The front wheels steer.
 * <p>
 * Extends VehicleModel directly (like the Puma): FCPVehicleModel un-hides transform bones, which would cancel the
 * missile hide.
 */
public class HimarsModel extends VehicleModel<HimarsEntity> {

    private static final List<String> TUBES = List.of("group", "group2", "group3", "group4", "group5", "group6");
    /** Wheel radius (blocks): axle height. */
    private static final double WHEEL_RADIUS = 0.641;

    @Override
    public ResourceLocation getModelResource(HimarsEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/himars.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<HimarsEntity> collectTransform(String boneName) {
        int tube = TUBES.indexOf(boneName);
        if (tube >= 0) return (bone, vehicle, state) -> bone.setHidden(vehicle.missilesLoaded() <= tube);

        VehicleModel.TransformContext<HimarsEntity> turn = WheelRotationTransforms.matchAnyTurn(boneName, WHEEL_RADIUS, 30f, "whell", "whell2");
        if (turn != null) return turn;
        VehicleModel.TransformContext<HimarsEntity> roll = WheelRotationTransforms.matchAny(boneName, WHEEL_RADIUS, "whell3", "whell4", "whell5", "whell6");
        if (roll != null) return roll;

        return super.collectTransform(boneName);
    }
}
