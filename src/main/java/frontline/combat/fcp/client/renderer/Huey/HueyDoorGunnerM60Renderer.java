package frontline.combat.fcp.client.renderer.Huey;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Huey.HueyDoorGunnerM60Model;
import frontline.combat.fcp.entity.vehicle.Huey.HueyDoorGunnerM60Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class HueyDoorGunnerM60Renderer extends VehicleRenderer<HueyDoorGunnerM60Entity> {
    public HueyDoorGunnerM60Renderer(EntityRendererProvider.Context renderManager) {super(renderManager, new HueyDoorGunnerM60Model());}

    @Override
    public ResourceLocation getTextureLocation(HueyDoorGunnerM60Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
