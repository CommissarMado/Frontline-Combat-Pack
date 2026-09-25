package frontline.combat.fcp.client.renderer.Uaz;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Uaz.UAZ452Model;
import frontline.combat.fcp.entity.vehicle.Uaz.UAZ452Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class UAZ452Renderer extends VehicleRenderer<UAZ452Entity> {

    public UAZ452Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new UAZ452Model());}

    @Override
    public ResourceLocation getTextureLocation(UAZ452Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
