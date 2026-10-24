package frontline.combat.fcp.client.model.Khrizantema;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Khrizantema.KhrizantemaEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;

import static frontline.combat.fcp.entity.vehicle.Khrizantema.KhrizantemaEntity.*;

/**
 * 9P157-2 Khrizantema-S. The launcher's deploy animations are driven by the entity's synced sight and
 * arm progress (see KhrizantemaEntity). Angles below are Blockbench degrees added to each bone's
 * rest rotation from the geo; the geo is modelled with the launcher deployed and its stowed pose is
 * the bones' rest rotations, so "deployed" = those rotations taken back to zero.
 * <p>
 * No track or wheel animation: none of the running gear bones match SBW's wheel/track names.
 * Extends VehicleModel directly (like the Puma): FCPVehicleModel un-hides transform bones, which
 * would cancel the missile hide.
 */
public class KhrizantemaModel extends VehicleModel<KhrizantemaEntity> {

    private static final float DOOR3_OPEN = 90f, DOOR4_OPEN = -90f, HATCH7_OPEN = -180f;
    /** Rise of the sight mast and of the launcher arm, in model pixels. */
    private static final float MAST_RISE = 13.6f, ARM_RISE = 9.35f;

    @Override
    public ResourceLocation getModelResource(KhrizantemaEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/khrizantema.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<KhrizantemaEntity> collectTransform(String boneName) {
        switch (boneName) {
            case "DOR3":
                return (bone, v, s) -> bone.setRotY(restY(bone) + DOOR3_OPEN * Mth.DEG_TO_RAD * doors(v, s.getPartialTick()));
            case "DOR4":
                return (bone, v, s) -> bone.setRotY(restY(bone) + DOOR4_OPEN * Mth.DEG_TO_RAD * doors(v, s.getPartialTick()));
            case "turret2":
                // Sight mast: rises, then turns with the gunner's look once up.
                return (bone, v, s) -> {
                    bone.setPosY(MAST_RISE * mast(v, s.getPartialTick()));
                    bone.setRotY(restY(bone) + v.mastYaw(s.getPartialTick()) * Mth.DEG_TO_RAD);
                };
            case "barrel2":
                return (bone, v, s) -> bone.setRotX(restX(bone) + v.mastPitch(s.getPartialTick()) * Mth.DEG_TO_RAD);
            case "hatch1":
                return (bone, v, s) -> bone.setRotX(restX(bone) * (1f - mast(v, s.getPartialTick())));
            case "hatch7":
                return (bone, v, s) -> bone.setRotZ(restZ(bone) + HATCH7_OPEN * Mth.DEG_TO_RAD * hatch(v, s.getPartialTick()));
            case "turret": {
                // Arm: rises and swings up out of the hull, then traverses (SBW's turret yaw).
                VehicleModel.TransformContext<KhrizantemaEntity> yaw = super.collectTransform("turret");
                return (bone, v, s) -> {
                    if (yaw != null) yaw.transform(bone, v, s);
                    float a = arm(v, s.getPartialTick());
                    bone.setPosY(ARM_RISE * a);
                    bone.setRotX(restX(bone) * (1f - a));
                };
            }
            case "barrel": {
                // Launcher: unfolds with the arm, then elevates (SBW's barrel pitch).
                VehicleModel.TransformContext<KhrizantemaEntity> pitch = super.collectTransform("barrel");
                return (bone, v, s) -> {
                    if (pitch != null) pitch.transform(bone, v, s);
                    bone.setRotX(bone.getRotX() + restX(bone) * (1f - arm(v, s.getPartialTick())));
                };
            }
            case "turret3":
                // Gunner's sight behind the doors: follows the gunner's look, also during reloads (see "SightBarrel").
                return (bone, v, s) -> bone.setRotY(restY(bone) + v.sightYaw(s.getPartialTick()) * Mth.DEG_TO_RAD);
            case "barrel3":
                return (bone, v, s) -> bone.setRotX(restX(bone) + v.sightPitch(s.getPartialTick()) * Mth.DEG_TO_RAD);
            case "9m123":
                // Magazine of 2 (BoundUpWithAmmoAmount): "9m123"'s tube fires first.
                return (bone, v, s) -> bone.setHidden(v.GetWeaponState(ATGM_WEAPON, 1));
            case "9m123_1":
                return (bone, v, s) -> bone.setHidden(v.GetWeaponState(ATGM_WEAPON, 0));
            case "pkt":
                // Bow PKT: one bone, yaw and pitch about its pivot (BoundBones math).
                return (bone, v, s) -> {
                    bone.setRotY(v.pktYaw(s.getPartialTick()) * Mth.DEG_TO_RAD);
                    bone.setRotX(v.pktPitch(s.getPartialTick()) * Mth.DEG_TO_RAD);
                };
            default:
                return super.collectTransform(boneName);
        }
    }

    // ---- timeline -> 0..1 progress of each part, eased like a powered drive --------------------

    private static float doors(KhrizantemaEntity v, float pt) {
        return ease(v.sight(pt) / DOORS);
    }

    private static float mast(KhrizantemaEntity v, float pt) {
        return ease(v.sight(pt) / MAST);
    }

    private static float hatch(KhrizantemaEntity v, float pt) {
        return ease(v.arm(pt) / HATCH);
    }

    private static float arm(KhrizantemaEntity v, float pt) {
        return ease((v.arm(pt) - HATCH / 2) / (LAUNCHER - HATCH / 2));
    }

    private static float ease(float t) {
        t = Mth.clamp(t, 0f, 1f);
        return t * t * (3f - 2f * t);
    }

    private static float restX(CoreGeoBone bone) {
        return bone.getInitialSnapshot().getRotX();
    }

    private static float restY(CoreGeoBone bone) {
        return bone.getInitialSnapshot().getRotY();
    }

    private static float restZ(CoreGeoBone bone) {
        return bone.getInitialSnapshot().getRotZ();
    }
}
