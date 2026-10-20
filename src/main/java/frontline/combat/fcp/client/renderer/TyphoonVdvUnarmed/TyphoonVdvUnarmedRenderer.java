package frontline.combat.fcp.client.renderer.TyphoonVdvUnarmed;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.TyphoonVdvUnarmed.TyphoonVdvUnarmedModel;
import frontline.combat.fcp.entity.vehicle.TyphoonVdvUnarmed.TyphoonVdvUnarmedEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class TyphoonVdvUnarmedRenderer extends VehicleRenderer<TyphoonVdvUnarmedEntity> {

    public TyphoonVdvUnarmedRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new TyphoonVdvUnarmedModel());}

    @Override
    public ResourceLocation getTextureLocation(TyphoonVdvUnarmedEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
