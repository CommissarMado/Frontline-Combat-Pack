package frontline.combat.fcp.client.renderer.Aavp;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Aavp.AAVPModel;
import frontline.combat.fcp.entity.vehicle.Aavp.AAVPEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class AAVPRenderer extends VehicleRenderer<AAVPEntity> {

    public AAVPRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new AAVPModel());}

    @Override
    public ResourceLocation getTextureLocation(AAVPEntity entity) {
        return entity.getCurrentTexture();
    }
}
