package frontline.combat.fcp.client.model.Emplacement;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.FCPVehicleModel;
import frontline.combat.fcp.entity.vehicle.Emplacement.EmplZis3Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class EmplZis3Model extends FCPVehicleModel<EmplZis3Entity> {
    @Override public ResourceLocation getModelResource(EmplZis3Entity a) {return new ResourceLocation(FCP.MODID, "geo/empl_zis3.geo.json");}
    @Override public boolean hideForTurretControllerWhileZooming() {return false;}
    @Override public @Nullable VehicleModel.TransformContext<EmplZis3Entity> collectTransform(String boneName) {
        // Breach recoil/reload: drop the breach bone 2.4 Blockbench units when unloaded (sign/scale may need tuning).
        if ("breach".equals(boneName)) {
            return (bone, vehicle, animationState) -> bone.setPosY(vehicle.isLoaded() ? 0f : -2.4f);
        }
        return super.collectTransform(boneName);
    }
}
