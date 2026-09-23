package frontline.combat.fcp.client.model.Uh60;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Uh60.Uh60MinigunEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class Uh60MinigunModel extends VehicleModel<Uh60MinigunEntity> {

    @Override
    public ResourceLocation getModelResource(Uh60MinigunEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/uh60_minigun.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<Uh60MinigunEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "vint" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            case "vint2" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // "turret" / "barrel" (right door gun) and "turret2" / "barrel2" (left door
            // gun) are each auto-driven by the base VehicleModel as independent main
            // turrets (TurretPos/BarrelPos and Turret2Pos/Barrel2Pos in
            // uh60_minigun.json) - both intentionally left to the default case below.
            // "bone" / "bone2" carry a static rest-pose rotation baked into the geo
            // file itself (correcting the imported mesh to face forward) and need no
            // Java handling either. Only the two guns' independent barrel-spin needs a
            // custom case here, since that's driven by each side's own ammo tracker.
            case "GUN" ->
                    (bone, vehicle, state) -> bone.setRotZ(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRotRightOld(), vehicle.getBarrelRotRight()));
            case "GUN2" ->
                    (bone, vehicle, state) -> bone.setRotZ(-Mth.lerp(state.getPartialTick(), vehicle.getBarrelRotLeftOld(), vehicle.getBarrelRotLeft()));
            default -> super.collectTransform(boneName);
        };
    }
}
