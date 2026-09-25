package frontline.combat.fcp.client.renderer.Huey;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Huey.HueyM134GunshipModel;
import frontline.combat.fcp.entity.vehicle.Huey.HueyM134GunshipEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class HueyM134GunshipRenderer extends VehicleRenderer<HueyM134GunshipEntity> {
    public HueyM134GunshipRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new HueyM134GunshipModel());}

    @Override
    public ResourceLocation getTextureLocation(HueyM134GunshipEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
