package frontline.combat.fcp.client.renderer.Humvee;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Humvee.HumveeModel;
import frontline.combat.fcp.client.model.Humvee.HumveeTOWModel;
import frontline.combat.fcp.client.renderer.FCPVehicleRenderer;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeEntity;
import frontline.combat.fcp.entity.vehicle.Humvee.HumveeTOWEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class HumveeTOWRenderer extends FCPVehicleRenderer<HumveeTOWEntity> {

    public HumveeTOWRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new HumveeTOWModel());}

    @Override
    protected float tiltStrength() {
        return 0.25f; // (0 = flat, 1 = SBW default)
    }

    @Override
    public ResourceLocation getTextureLocation(HumveeTOWEntity entity) {
        return entity.getCurrentTexture();
    }
}
