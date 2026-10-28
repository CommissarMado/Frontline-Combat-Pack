package frontline.combat.fcp.client.model.Civ;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.Util.WheelRotationTransforms;
import frontline.combat.fcp.entity.vehicle.Civ.CivCarEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/** One model for the civilian cars: geo by registry name; front wheels steer and roll, rear wheels roll. */
public class CivCarModel extends VehicleModel<CivCarEntity> {

    /** Wheel radius (blocks, = axle height) and the front / rear wheel bones per car. */
    private record Wheels(double radius, String[] front, String[] rear) {}

    private static final Wheels DEFAULT = new Wheels(0.45, new String[]{"wheel1", "wheel2"}, new String[]{"wheel3", "wheel4"});
    private static final Map<String, Wheels> WHEELS = Map.of(
            "bmw_e34", new Wheels(0.36, new String[]{"WHEEL2", "WHEEL4"}, new String[]{"WHEEL3", "WHEEL5"}),
            "land_cruiser_80", new Wheels(0.45, DEFAULT.front, DEFAULT.rear),
            "suburban", new Wheels(0.47, DEFAULT.front, DEFAULT.rear),
            "vw_t4", new Wheels(0.37, DEFAULT.front, DEFAULT.rear));

    @Override
    public ResourceLocation getModelResource(CivCarEntity animatable) {
        return new ResourceLocation(FCP.MODID, "geo/" + animatable.variant() + ".geo.json");
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable VehicleModel.TransformContext<CivCarEntity> collectTransform(String boneName) {
        // Bone names differ per car, so every car's wheel bones are wired; each geo only has its own.
        for (Wheels w : WHEELS.values()) {
            if (java.util.List.of(w.front).contains(boneName)) return wheelsFor(boneName, true);
            if (java.util.List.of(w.rear).contains(boneName)) return wheelsFor(boneName, false);
        }
        return super.collectTransform(boneName);
    }

    /** The wheel transform with the radius of the car actually being drawn (shared bone names, different sizes). */
    private VehicleModel.TransformContext<CivCarEntity> wheelsFor(String boneName, boolean front) {
        return (bone, vehicle, state) -> {
            Wheels w = WHEELS.getOrDefault(vehicle.variant(), DEFAULT);
            VehicleModel.TransformContext<CivCarEntity> t = front
                    ? WheelRotationTransforms.matchAnyTurn(boneName, w.radius, 30f, w.front)
                    : WheelRotationTransforms.matchAny(boneName, w.radius, w.rear);
            if (t != null) t.transform(bone, vehicle, state);
        };
    }
}
