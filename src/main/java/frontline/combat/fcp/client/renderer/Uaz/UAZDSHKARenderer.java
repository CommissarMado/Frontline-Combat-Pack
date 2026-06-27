package frontline.combat.fcp.client.renderer.Uaz;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Uaz.UAZDSHKAModel;
import frontline.combat.fcp.client.model.Uaz.UAZModel;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZDSHKAEntity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class UAZDSHKARenderer extends VehicleRenderer<UAZDSHKAEntity> {

    public UAZDSHKARenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new UAZDSHKAModel());}

    @Override
    public ResourceLocation getTextureLocation(UAZDSHKAEntity entity) {
        return entity.getCurrentTexture();
    }
}
