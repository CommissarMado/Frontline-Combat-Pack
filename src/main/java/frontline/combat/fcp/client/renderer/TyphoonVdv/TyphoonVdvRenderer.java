package frontline.combat.fcp.client.renderer.TyphoonVdv;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.TyphoonVdv.TyphoonVdvModel;
import frontline.combat.fcp.entity.vehicle.TyphoonVdv.TyphoonVdvEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class TyphoonVdvRenderer extends VehicleRenderer<TyphoonVdvEntity> {

    public TyphoonVdvRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new TyphoonVdvModel());}

    @Override
    public ResourceLocation getTextureLocation(TyphoonVdvEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
