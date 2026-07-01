package frontline.combat.fcp.client.renderer.Matv;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Matv.MATV9In1Model;
import frontline.combat.fcp.client.model.Matv.MATVModel;
import frontline.combat.fcp.entity.vehicle.Matv.MATV9In1Entity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MATV9In1Renderer extends VehicleRenderer<MATV9In1Entity> {

    public MATV9In1Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new MATV9In1Model());}

    @Override
    public ResourceLocation getTextureLocation(MATV9In1Entity entity) {
        return entity.getCurrentTexture();
    }
}
