package frontline.combat.fcp.client.renderer.Himars;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Himars.HimarsModel;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.Himars.HimarsEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class HimarsRenderer extends VehicleRenderer<HimarsEntity> {

    public HimarsRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new HimarsModel()); }

    @Override
    public ResourceLocation getTextureLocation(HimarsEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
