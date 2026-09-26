package frontline.combat.fcp.client.renderer.Littlebird;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Littlebird.Ah6mLittlebirdModel;
import frontline.combat.fcp.entity.vehicle.Littlebird.Ah6mLittlebirdEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class Ah6mLittlebirdRenderer extends VehicleRenderer<Ah6mLittlebirdEntity> {
    public Ah6mLittlebirdRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new Ah6mLittlebirdModel());}

    @Override
    public ResourceLocation getTextureLocation(Ah6mLittlebirdEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
