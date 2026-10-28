package frontline.combat.fcp.client.renderer.T90M;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.T90M.T90MModel;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.T90M.T90MEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class T90MRenderer extends VehicleRenderer<T90MEntity> {

    public T90MRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new T90MModel()); }

    @Override
    public ResourceLocation getTextureLocation(T90MEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
