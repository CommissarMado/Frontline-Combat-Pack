package frontline.combat.fcp.client.renderer.JohnDeere;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.JohnDeere.JohnDeereModel;
import frontline.combat.fcp.client.model.Matv.MATVModel;
import frontline.combat.fcp.entity.vehicle.JohnDeere.JohnDeereEntity;
import frontline.combat.fcp.entity.vehicle.Matv.MATVEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class JohnDeereRenderer extends VehicleRenderer<JohnDeereEntity> {

    public JohnDeereRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new JohnDeereModel());}

    @Override
    public ResourceLocation getTextureLocation(JohnDeereEntity entity) {
        return entity.getCurrentTexture();
    }
}
