package frontline.combat.fcp.client.renderer.Novator;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Novator.NovatorModel;
import frontline.combat.fcp.client.renderer.FCPVehicleRenderer;
import frontline.combat.fcp.entity.vehicle.Novator.NovatorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class NovatorRenderer extends FCPVehicleRenderer<NovatorEntity> {
    public NovatorRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new NovatorModel());}

    @Override
    protected float tiltStrength() {
        return 0.25f; // (0 = flat, 1 = SBW default)
    }

    @Override
    public ResourceLocation getTextureLocation(NovatorEntity entity) {
        return entity.getCurrentTexture();
    }
}
