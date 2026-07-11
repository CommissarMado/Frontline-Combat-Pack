package frontline.combat.fcp.client.renderer.Bmp1;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp1.BMP1Model;
import frontline.combat.fcp.entity.vehicle.Bmp1.BMP1Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BMP1Renderer extends VehicleRenderer<BMP1Entity> {

    public BMP1Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new BMP1Model());}

    @Override
    public ResourceLocation getTextureLocation(BMP1Entity entity) {
        return entity.getCurrentTexture();
    }
}
