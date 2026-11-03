package frontline.combat.fcp.client.renderer.Ka52;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Ka52.Ka52Model;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.Ka52.Ka52Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class Ka52Renderer extends VehicleRenderer<Ka52Entity> {

    public Ka52Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new Ka52Model()); }

    @Override
    public ResourceLocation getTextureLocation(Ka52Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
