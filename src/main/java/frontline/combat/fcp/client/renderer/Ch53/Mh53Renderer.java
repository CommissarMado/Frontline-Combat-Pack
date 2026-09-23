package frontline.combat.fcp.client.renderer.Ch53;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Ch53.Mh53Model;
import frontline.combat.fcp.entity.vehicle.Ch53.Mh53Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class Mh53Renderer extends VehicleRenderer<Mh53Entity> {

    public Mh53Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Mh53Model());
    }

    @Override
    public ResourceLocation getTextureLocation(Mh53Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
