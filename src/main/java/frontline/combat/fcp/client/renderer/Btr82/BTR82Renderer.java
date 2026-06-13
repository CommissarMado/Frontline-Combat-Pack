package frontline.combat.fcp.client.renderer.Btr82;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp.BMP1Model;
import frontline.combat.fcp.client.model.Btr82.BTR82Model;
import frontline.combat.fcp.entity.vehicle.Bmp.BMP1Entity;
import frontline.combat.fcp.entity.vehicle.Btr82.BTR82Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BTR82Renderer extends VehicleRenderer<BTR82Entity> {

    public BTR82Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new BTR82Model());}

    @Override
    public ResourceLocation getTextureLocation(BTR82Entity entity) {
        return entity.getCurrentTexture();
    }
}
