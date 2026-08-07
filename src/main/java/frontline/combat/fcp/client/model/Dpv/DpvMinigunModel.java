package frontline.combat.fcp.client.model.Dpv;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Dpv.DpvMinigunEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class DpvMinigunModel extends VehicleModel<DpvMinigunEntity> {
    @Override
    public ResourceLocation getModelResource(DpvMinigunEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/dpv_minigun.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {return false;}

    @Override
    public @Nullable VehicleModel.TransformContext<DpvMinigunEntity> collectTransform(String boneName) {

        // Front wheels wheel1/wheel3 (Z ~ -1.40): steer + roll. Rear wheel2/wheel4 (Z ~ +2.01): roll only.
        VehicleModel.TransformContext<DpvMinigunEntity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.58, 30f, "wheel1", "wheel3");
        if (turn != null) return turn;

        VehicleModel.TransformContext<DpvMinigunEntity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.58, "wheel2", "wheel4");
        if (wheels != null) return wheels;

        // Pintle weapon: the "barrel" bone is a root bone that both traverses (yaw) and elevates
        // (pitch), like the tripod emplacements' single-bone mounts. The seat stays put; only this
        // bone and the camera move. NOTE: the geo bakes a rotation into "barrel", so if yaw or pitch
        // comes out inverted in-game, flip the corresponding sign here.
        if ("barrel".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float pt = (float) state.getPartialTick();
                float yaw = Mth.lerp(pt, vehicle.getTurretYRotO(), vehicle.getTurretYRot());
                float pitch = Mth.lerp(pt, vehicle.getTurretXRotO(), vehicle.getTurretXRot());
                bone.setRotY(yaw * Mth.DEG_TO_RAD);
                bone.setRotX(-Mth.clamp(pitch, vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD);
            };
        }

        return super.collectTransform(boneName);
    }
}
