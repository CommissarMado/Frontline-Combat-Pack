package frontline.combat.fcp.client.renderer.Toyota;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Toyota.ToyotaHiluxBMPModel;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxBMPEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ToyotaHiluxBMPRenderer extends VehicleRenderer<ToyotaHiluxBMPEntity> {

    public ToyotaHiluxBMPRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new ToyotaHiluxBMPModel());}

    @Override
    public ResourceLocation getTextureLocation(ToyotaHiluxBMPEntity entity) {
        return entity.getCurrentTexture();
    }
}
