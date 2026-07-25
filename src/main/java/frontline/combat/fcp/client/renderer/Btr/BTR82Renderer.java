package frontline.combat.fcp.client.renderer.Btr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Btr.BTR82Model;
import frontline.combat.fcp.entity.vehicle.Btr.BTR82Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BTR82Renderer extends VehicleRenderer<BTR82Entity> {

    public BTR82Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new BTR82Model());}

    @Override
    public ResourceLocation getTextureLocation(BTR82Entity entity) {
        return entity.getCurrentTexture();
    }
}
