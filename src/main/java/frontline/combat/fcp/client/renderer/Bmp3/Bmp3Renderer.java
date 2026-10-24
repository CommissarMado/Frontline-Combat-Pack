package frontline.combat.fcp.client.renderer.Bmp3;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp3.Bmp3Model;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.Bmp3.Bmp3Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class Bmp3Renderer extends VehicleRenderer<Bmp3Entity> {

    public Bmp3Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Bmp3Model());
    }

    @Override
    public ResourceLocation getTextureLocation(Bmp3Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
