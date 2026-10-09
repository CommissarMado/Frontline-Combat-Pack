package frontline.combat.fcp.client.model.Bmp.Bmp2;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Bmp.BmpTrackPaths;
import frontline.combat.fcp.client.model.Util.FCPTrackPath;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.client.model.Util.CannonRecoilTransforms;
import frontline.combat.fcp.client.model.Util.ModelBoneTransforms;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp2.BMP2MDEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class BMP2MDModel extends FCPVehicleModel<BMP2MDEntity> {

    private static final String CANNON_WEAPON = "Cannon";

    @Override
    public ResourceLocation getModelResource(BMP2MDEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/bmp2md.geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }


    @Override
    public @Nullable VehicleModel.TransformContext<BMP2MDEntity> collectTransform(String boneName) {
        return switch (boneName) {

            case "BarrelOccilator" -> barrelRecoil(0);

            // Road wheels are named wheelL0-7 / wheelR0-7 and are driven by
            // SuperbWarfare's own wheel handler (pattern ^wheel[LR].*), which spins
            // each side from its real track speed - same as the base BMP-2.
            default -> super.collectTransform(boneName);
        };
    }

    private VehicleModel.TransformContext<BMP2MDEntity> barrelRecoil(int barrelIndex) {
        return (bone, vehicle, state) -> {
            ModelBoneTransforms.clearRecoilOffsets(bone);
            if (vehicle.getCannonRecoilTime() <= 0) {
                return;
            }
            if (!CANNON_WEAPON.equals(CannonRecoilTransforms.gunnerGunName(vehicle))) {
                return;
            }
            CannonRecoilTransforms.apply(bone, vehicle, CannonRecoilTransforms.Profile.STANDARD);
        };
    }

    @Override
    protected FCPTrackPath getTrackPath() {
        // Shared with every other BMP variant. Flip just this vehicle with .reversed(),
        // .rotationInverted() or .rotationOffset(180) - no table changes needed.
        return BmpTrackPaths.BMP.reversed();
    }
}
