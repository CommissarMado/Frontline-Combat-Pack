package frontline.combat.fcp.client.model.Util;

import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.state.BoneSnapshot;

public final class ModelBoneTransforms {

    private ModelBoneTransforms() {
    }

    /**
     * Clears per-frame overrides on shared GeckoLib bones before applying another vehicle's state.
     * Back to the bone's rest pose from the geo - not zero - so bones modelled with a rotation (e.g. the
     * BMP-1's tilted Malyutka) don't jump whenever a second vehicle of the same model renders.
     */
    public static void resetForVehicleRender(GeoBone bone) {
        BoneSnapshot rest = bone.getInitialSnapshot();
        bone.setHidden(false);
        bone.setPosX(rest.getOffsetX());
        bone.setPosY(rest.getOffsetY());
        bone.setPosZ(rest.getOffsetZ());
        bone.setRotX(rest.getRotX());
        bone.setRotY(rest.getRotY());
        bone.setRotZ(rest.getRotZ());
        bone.setScaleX(rest.getScaleX());
        bone.setScaleY(rest.getScaleY());
        bone.setScaleZ(rest.getScaleZ());
    }

    /** Clears the recoil kick (Z offset, X rotation) back to the bone's rest pose. */
    public static void clearRecoilOffsets(GeoBone bone) {
        BoneSnapshot rest = bone.getInitialSnapshot();
        bone.setPosZ(rest.getOffsetZ());
        bone.setRotX(rest.getRotX());
    }
}
