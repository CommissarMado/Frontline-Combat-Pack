package frontline.combat.fcp.client.renderer.Bmp1;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp1.BMP1UModel;
import frontline.combat.fcp.entity.vehicle.Bmp1.BMP1UEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BMP1URenderer extends VehicleRenderer<BMP1UEntity> {

    public BMP1URenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new BMP1UModel());}

    @Override
    public ResourceLocation getTextureLocation(BMP1UEntity entity) {
        return entity.getCurrentTexture();
    }
}
