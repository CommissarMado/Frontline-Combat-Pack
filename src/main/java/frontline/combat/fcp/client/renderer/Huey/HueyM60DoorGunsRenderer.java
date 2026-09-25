package frontline.combat.fcp.client.renderer.Huey;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Huey.HueyM60DoorGunsModel;
import frontline.combat.fcp.entity.vehicle.Huey.HueyM60DoorGunsEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class HueyM60DoorGunsRenderer extends VehicleRenderer<HueyM60DoorGunsEntity> {
    public HueyM60DoorGunsRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new HueyM60DoorGunsModel());}

    @Override
    public ResourceLocation getTextureLocation(HueyM60DoorGunsEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
