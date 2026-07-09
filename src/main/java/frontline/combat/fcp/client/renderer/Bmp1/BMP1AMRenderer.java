package frontline.combat.fcp.client.renderer.Bmp1;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp1.BMP1AMModel;
import frontline.combat.fcp.client.model.Bmp1.BMP1Model;
import frontline.combat.fcp.entity.vehicle.Bmp1.BMP1AMEntity;
import frontline.combat.fcp.entity.vehicle.Bmp1.BMP1Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BMP1AMRenderer extends VehicleRenderer<BMP1AMEntity> {

    public BMP1AMRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new BMP1AMModel());}

    @Override
    public ResourceLocation getTextureLocation(BMP1AMEntity entity) {
        return entity.getCurrentTexture();
    }
}
