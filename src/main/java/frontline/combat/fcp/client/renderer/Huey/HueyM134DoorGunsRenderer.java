package frontline.combat.fcp.client.renderer.Huey;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Huey.HueyM134DoorGunsModel;
import frontline.combat.fcp.entity.vehicle.Huey.HueyM134DoorGunsEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class HueyM134DoorGunsRenderer extends VehicleRenderer<HueyM134DoorGunsEntity> {
    public HueyM134DoorGunsRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new HueyM134DoorGunsModel());}

    @Override
    public ResourceLocation getTextureLocation(HueyM134DoorGunsEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
