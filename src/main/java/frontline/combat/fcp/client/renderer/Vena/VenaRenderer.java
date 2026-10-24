package frontline.combat.fcp.client.renderer.Vena;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Vena.VenaModel;
import frontline.combat.fcp.entity.vehicle.Vena.VenaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class VenaRenderer extends VehicleRenderer<VenaEntity> {

    public VenaRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new VenaModel());}

    @Override
    public ResourceLocation getTextureLocation(VenaEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
