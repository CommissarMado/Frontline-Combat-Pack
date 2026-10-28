package frontline.combat.fcp.client.renderer.Bradley;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bradley.BradleyModel;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.Bradley.BradleyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BradleyRenderer extends VehicleRenderer<BradleyEntity> {

    public BradleyRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new BradleyModel()); }

    @Override
    public ResourceLocation getTextureLocation(BradleyEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
