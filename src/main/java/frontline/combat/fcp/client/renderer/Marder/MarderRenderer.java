package frontline.combat.fcp.client.renderer.Marder;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Marder.MarderModel;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.Marder.MarderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MarderRenderer extends VehicleRenderer<MarderEntity> {

    public MarderRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new MarderModel()); }

    @Override
    public ResourceLocation getTextureLocation(MarderEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
