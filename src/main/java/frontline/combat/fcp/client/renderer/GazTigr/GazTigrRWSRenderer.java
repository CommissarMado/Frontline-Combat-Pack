package frontline.combat.fcp.client.renderer.GazTigr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.GazTigr.GazTigrRWSModel;
import frontline.combat.fcp.entity.vehicle.GazTigr.GazTigrRWSEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class GazTigrRWSRenderer extends VehicleRenderer<GazTigrRWSEntity> {
    public GazTigrRWSRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new GazTigrRWSModel());}

    @Override
    public ResourceLocation getTextureLocation(GazTigrRWSEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
