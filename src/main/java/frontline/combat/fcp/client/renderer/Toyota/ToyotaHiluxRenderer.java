package frontline.combat.fcp.client.renderer.Toyota;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Toyota.ToyotaHiluxModel;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class ToyotaHiluxRenderer extends VehicleRenderer<ToyotaHiluxEntity> {
    public ToyotaHiluxRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new ToyotaHiluxModel());}

    @Override
    public ResourceLocation getTextureLocation(ToyotaHiluxEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
