package frontline.combat.fcp.client.renderer.Toyota;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Toyota.ToyotaHiluxModel;
import frontline.combat.fcp.client.renderer.FCPVehicleRenderer;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ToyotaHiluxRenderer extends FCPVehicleRenderer<ToyotaHiluxEntity> {
    public ToyotaHiluxRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new ToyotaHiluxModel());}

    @Override
    protected float tiltStrength() {
        return 0.25f; // (0 = flat, 1 = SBW default)
    }

    @Override
    public ResourceLocation getTextureLocation(ToyotaHiluxEntity entity) {
        return entity.getCurrentTexture();
    }
}
