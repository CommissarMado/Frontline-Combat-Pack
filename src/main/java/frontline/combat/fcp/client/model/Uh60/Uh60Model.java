package frontline.combat.fcp.client.model.Uh60;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Uh60.Uh60Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class Uh60Model extends VehicleModel<Uh60Entity> {

    @Override
    public ResourceLocation getModelResource(Uh60Entity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/uh60.geo.json");
    }

    @Override
    public @Nullable VehicleModel.TransformContext<Uh60Entity> collectTransform(String boneName) {
        return switch (boneName) {
            // Main rotor spins about the mast (Y axis), driven by the shared engine
            // propeller rotation value every helicopter already has.
            case "vint" ->
                    (bone, vehicle, state) -> bone.setRotY(-Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            // Tail rotor spins about a lateral axis (X); geared faster than the main
            // rotor, matching the x6 ratio FCP's other helicopters use for their tail rotors.
            case "vint2" ->
                    (bone, vehicle, state) -> bone.setRotX(6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
            default -> super.collectTransform(boneName);
        };
    }
}
