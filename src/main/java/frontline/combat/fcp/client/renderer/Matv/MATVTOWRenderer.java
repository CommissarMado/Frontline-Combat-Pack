package frontline.combat.fcp.client.renderer.Matv;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Matv.MATV9In1Model;
import frontline.combat.fcp.client.model.Matv.MATVTOWModel;
import frontline.combat.fcp.entity.vehicle.Matv.MATV9In1Entity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVTOWEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MATVTOWRenderer extends VehicleRenderer<MATVTOWEntity> {

    public MATVTOWRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new MATVTOWModel());}

    @Override
    public ResourceLocation getTextureLocation(MATVTOWEntity entity) {
        return entity.getCurrentTexture();
    }
}

