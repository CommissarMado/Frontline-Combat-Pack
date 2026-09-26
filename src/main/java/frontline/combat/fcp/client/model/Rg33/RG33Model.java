package frontline.combat.fcp.client.model.Rg33;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Rg33.RG33Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class RG33Model extends VehicleModel<RG33Entity> {

    @Override
    public ResourceLocation getModelResource(RG33Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/rg_33.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<RG33Entity> collectTransform(String boneName) {

        // Front axle (whell/whell2) steers; the geo has no separate "...Turn" sub-bone, so the
        // wheel bone itself is what rolls + pivots, same convention as the UAZ's.
        VehicleModel.TransformContext<RG33Entity> turn =
                WheelRotationTransforms.matchAnyTurn(boneName, 0.5547, 35f,
                        "whell", "whell2");
        if (turn != null) return turn;

        // Both rear tandem axles (whell3/whell4/whell5/whell6) just roll.
        VehicleModel.TransformContext<RG33Entity> wheels =
                WheelRotationTransforms.matchAny(boneName, 0.5547,
                        "whell3", "whell4", "whell5", "whell6");
        if (wheels != null) return wheels;

        // The "turret" bone was authored in Blockbench with a baked rest rotation of
        // (-180, 0, 180) (visible in the geo's "rotation" field) instead of (0,0,0).
        // SBW's base VehicleModel only ever writes bone.rotY for this bone (super just
        // does bone.rotY = turretYRot) and never touches rotX/rotZ, so that baked
        // -180/180 stays applied underneath whatever yaw we drive.
        //
        // GeckoLib's own bone loader (BakedModelFactory) negates the X and Y rotation
        // components (but not Z) when it reads a bone's authored rotation, and negates
        // the X pivot component (but not Y/Z) when it reads a bone's pivot - and then
        // composes local rotation as Rz*Ry*Rx (RenderUtils.rotateMatrixAroundBone).
        // Working through that composition with this bone's specific baked values
        // shows the visible yaw ends up being the exact NEGATION of whatever we feed
        // bone.rotY (not a +-180 offset - that was tried and confirmed wrong: it left
        // the turret facing backwards at rest AND spinning the wrong way). Negating is
        // the correct, minimal compensation and matches what was already shipped here
        // before an unnecessary "+180" was added - reverting that change.
        //
        // This sign flip carries through to BarrelPos in rg_33.json too: with rotY
        // negated, the barrel bone's rendered pivot (relative to TurretPos) comes out
        // as the geo's raw pivot delta with X and Z BOTH negated from the naive
        // pixel/16 conversion, not the plain conversion used for a non-rotated bone.
        // See rg_33.json's BarrelPos for the corrected value - the two changes only
        // work together. Do NOT touch the geo's baked rotation - compensating here
        // keeps the artist's original model file intact.
        if ("turret".equals(boneName)) {
            return (bone, vehicle, state) -> {
                bone.setRotY((float) Math.toRadians(-getTurretYRot()));
                bone.setHidden(vehicle.isWreck() && vehicle.hasTurret() && vehicle.getSympatheticDetonated());
            };
        }

        return super.collectTransform(boneName);
    }
}
