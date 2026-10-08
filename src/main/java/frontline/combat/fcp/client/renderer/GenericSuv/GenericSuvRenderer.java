package frontline.combat.fcp.client.renderer.GenericSuv;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.GenericSuv.GenericSuvModel;
import frontline.combat.fcp.entity.vehicle.GenericSuv.GenericSuvEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class GenericSuvRenderer extends VehicleRenderer<GenericSuvEntity> {

    public GenericSuvRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new GenericSuvModel());}

    @Override
    public ResourceLocation getTextureLocation(GenericSuvEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
