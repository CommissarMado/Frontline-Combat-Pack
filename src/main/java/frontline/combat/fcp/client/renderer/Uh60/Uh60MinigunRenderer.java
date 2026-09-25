package frontline.combat.fcp.client.renderer.Uh60;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Uh60.Uh60MinigunModel;
import frontline.combat.fcp.entity.vehicle.Uh60.Uh60MinigunEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class Uh60MinigunRenderer extends VehicleRenderer<Uh60MinigunEntity> {

    public Uh60MinigunRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Uh60MinigunModel());
    }

    @Override
    public ResourceLocation getTextureLocation(Uh60MinigunEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
