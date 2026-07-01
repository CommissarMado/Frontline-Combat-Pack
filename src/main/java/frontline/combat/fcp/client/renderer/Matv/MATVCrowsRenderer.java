package frontline.combat.fcp.client.renderer.Matv;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Matv.MATVCrowsModel;
import frontline.combat.fcp.client.model.Matv.MATVTOWModel;
import frontline.combat.fcp.entity.vehicle.Matv.MATVCrowsEntity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVTOWEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MATVCrowsRenderer extends VehicleRenderer<MATVCrowsEntity> {

    public MATVCrowsRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new MATVCrowsModel());}

    @Override
    public ResourceLocation getTextureLocation(MATVCrowsEntity entity) {
        return entity.getCurrentTexture();
    }
}
