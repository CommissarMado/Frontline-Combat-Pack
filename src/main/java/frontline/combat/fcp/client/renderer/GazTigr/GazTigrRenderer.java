package frontline.combat.fcp.client.renderer.GazTigr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.GazTigr.GazTigrModel;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class GazTigrRenderer extends VehicleRenderer<GazTigrEntity> {
    public GazTigrRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GazTigrModel());
    }

    @Override
    public ResourceLocation getTextureLocation(GazTigrEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
