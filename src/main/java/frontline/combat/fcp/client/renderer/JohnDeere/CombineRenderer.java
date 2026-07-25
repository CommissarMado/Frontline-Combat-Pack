package frontline.combat.fcp.client.renderer.JohnDeere;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.JohnDeere.CombineModel;
import frontline.combat.fcp.client.model.JohnDeere.JohnDeereModel;
import frontline.combat.fcp.entity.vehicle.JohnDeere.CombineEntity;
import frontline.combat.fcp.entity.vehicle.JohnDeere.JohnDeereEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CombineRenderer extends VehicleRenderer<CombineEntity> {

    public CombineRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new CombineModel());}

    @Override
    public ResourceLocation getTextureLocation(CombineEntity entity) {
        return entity.getCurrentTexture();
    }
}
