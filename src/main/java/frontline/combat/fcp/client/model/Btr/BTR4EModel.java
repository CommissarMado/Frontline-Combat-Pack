package frontline.combat.fcp.client.model.Btr;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Btr.BTR4EEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class BTR4EModel extends FCPVehicleModel<BTR4EEntity> {

    private static final double WHEEL_RADIUS = 0.584;

    @Override public ResourceLocation getModelResource(BTR4EEntity a) { return new ResourceLocation(FCP.MODID, "geo/btr4e.geo.json"); }
    @Override public boolean hideForTurretControllerWhileZooming() { return false; }

    @Override
    public @Nullable VehicleModel.TransformContext<BTR4EEntity> collectTransform(String boneName) {
        // ATGM launchers sit on the turret, not the barrel: elevate them with the gun.
        if ("D".equals(boneName) || "T".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float xRot = Mth.lerp(state.getPartialTick(), vehicle.getTurretXRotO(), vehicle.getTurretXRot());
                bone.setRotX(Mth.clamp(-xRot, vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);
            };
        }

        // The wheel bones are modelled with a 180° yaw, so the shared helpers' roll/steer need
        // the yaw restored and the roll flipped. Front two axles steer; rear two only roll.
        VehicleModel.TransformContext<BTR4EEntity> steer =
                WheelRotationTransforms.matchAnyTurn(boneName, WHEEL_RADIUS, 30f,
                        "whell1", "whell2", "whell8", "whell5");
        if (steer != null) return yawFlipped(steer, true);
        VehicleModel.TransformContext<BTR4EEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, WHEEL_RADIUS,
                        "whell3", "whell4", "whell6", "whell7");
        if (wheels != null) return yawFlipped(wheels, false);

        return super.collectTransform(boneName);
    }

    private static VehicleModel.TransformContext<BTR4EEntity> yawFlipped(
            VehicleModel.TransformContext<BTR4EEntity> inner, boolean steered) {
        return (bone, vehicle, state) -> {
            inner.transform(bone, vehicle, state);
            bone.setRotX(-bone.getRotX());
            bone.setRotY((steered ? bone.getRotY() : 0f) + Mth.PI);
        };
    }
}
