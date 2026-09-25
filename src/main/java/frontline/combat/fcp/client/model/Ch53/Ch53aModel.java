package frontline.combat.fcp.client.model.Ch53;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Ch53.Ch53aEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class Ch53aModel extends VehicleModel<Ch53aEntity> {

    @Override
    public ResourceLocation getModelResource(Ch53aEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/ch53a.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<Ch53aEntity> collectTransform(String boneName) {
        return switch (boneName) {
            // Main rotor spins about the mast (Y axis), driven by the shared engine
            // propeller rotation value every helicopter already has.
            case "MainRotor" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // Tail rotor spins about a lateral axis (X); geared faster than the main
            // rotor, matching the x6 ratio FCP's other helicopters use for their tail rotors.
            case "RearRotor" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            default -> super.collectTransform(boneName);
        };
    }
}
