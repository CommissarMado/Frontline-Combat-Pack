package frontline.combat.fcp.client.renderer.Bmp.Bmp1;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp.Bmp1.BMP1Model;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp1.BMP1Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class BMP1Renderer extends VehicleRenderer<BMP1Entity> {

    public BMP1Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new BMP1Model());}

    @Override
    public ResourceLocation getTextureLocation(BMP1Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
