package frontline.combat.fcp.client.renderer.Msta;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.M109.M109Model;
import frontline.combat.fcp.client.model.Msta.MstaModel;
import frontline.combat.fcp.entity.vehicle.M109.M109Entity;
import frontline.combat.fcp.entity.vehicle.Msta.MstaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MstaRenderer extends VehicleRenderer<MstaEntity> {

    public MstaRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new MstaModel());}

    @Override
    public ResourceLocation getTextureLocation(MstaEntity entity) {
        return entity.getCurrentTexture();
    }
}
