package frontline.combat.fcp.client.renderer.Type10;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Abrams.M1a1Model;
import frontline.combat.fcp.client.model.Type10.Type10Model;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.Abrams.M1a1Entity;
import frontline.combat.fcp.entity.vehicle.Type10.Type10Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class Type10Renderer extends VehicleRenderer<Type10Entity> {

    public Type10Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new Type10Model());}

    @Override
    public ResourceLocation getTextureLocation(Type10Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
