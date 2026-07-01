package frontline.combat.fcp.client.renderer.Mi17;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Mi17.MI17Model;
import frontline.combat.fcp.entity.vehicle.Mi17.MI17Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MI17Renderer extends VehicleRenderer<MI17Entity> {

    public MI17Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MI17Model());
    }

    @Override
    public ResourceLocation getTextureLocation(MI17Entity entity) {
        return entity.getCurrentTexture();
    }
}