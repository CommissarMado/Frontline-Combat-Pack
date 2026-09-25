package frontline.combat.fcp.client.renderer.Uh60;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Uh60.Mh60lModel;
import frontline.combat.fcp.entity.vehicle.Uh60.Mh60lEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class Mh60lRenderer extends VehicleRenderer<Mh60lEntity> {

    public Mh60lRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Mh60lModel());
    }

    @Override
    public ResourceLocation getTextureLocation(Mh60lEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
