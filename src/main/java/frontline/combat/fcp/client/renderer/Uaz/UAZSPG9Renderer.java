package frontline.combat.fcp.client.renderer.Uaz;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Uaz.UAZSPG9Model;
import frontline.combat.fcp.client.model.Uaz.UAZModel;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZSPG9Entity;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class UAZSPG9Renderer extends VehicleRenderer<UAZSPG9Entity> {

    public UAZSPG9Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new UAZSPG9Model());}

    @Override
    public ResourceLocation getTextureLocation(UAZSPG9Entity entity) {
        return entity.getCurrentTexture();
    }
}
