package frontline.combat.fcp.client.renderer.Uaz;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Uaz.UAZModel;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class UAZRenderer extends VehicleRenderer<UAZEntity> {

    public UAZRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new UAZModel());}

    @Override
    public ResourceLocation getTextureLocation(UAZEntity entity) {
        return entity.getCurrentTexture();
    }
}
