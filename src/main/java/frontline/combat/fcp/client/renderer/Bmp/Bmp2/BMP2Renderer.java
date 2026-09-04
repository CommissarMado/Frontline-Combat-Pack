package frontline.combat.fcp.client.renderer.Bmp.Bmp2;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp.Bmp2.BMP2Model;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp2.BMP2Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class BMP2Renderer extends VehicleRenderer<BMP2Entity> {

    public BMP2Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new BMP2Model());}

    @Override
    public ResourceLocation getTextureLocation(BMP2Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
