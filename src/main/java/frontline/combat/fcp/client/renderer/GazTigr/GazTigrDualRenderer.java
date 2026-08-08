package frontline.combat.fcp.client.renderer.GazTigr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.GazTigr.GazTigrDualModel;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrDualEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class GazTigrDualRenderer extends VehicleRenderer<GazTigrDualEntity> {
    public GazTigrDualRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GazTigrDualModel());
    }

    @Override
    public ResourceLocation getTextureLocation(GazTigrDualEntity entity) {
        return entity.getCurrentTexture();
    }
}
