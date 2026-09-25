package frontline.combat.fcp.client.renderer.Huey;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Huey.HueyM60GunshipModel;
import frontline.combat.fcp.entity.vehicle.Huey.HueyM60GunshipEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class HueyM60GunshipRenderer extends VehicleRenderer<HueyM60GunshipEntity> {
    public HueyM60GunshipRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new HueyM60GunshipModel());}

    @Override
    public ResourceLocation getTextureLocation(HueyM60GunshipEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
