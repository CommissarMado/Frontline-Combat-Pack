package frontline.combat.fcp.client.renderer.Civ;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Civ.CivCarModel;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.Civ.CivCarEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CivCarRenderer extends VehicleRenderer<CivCarEntity> {

    public CivCarRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new CivCarModel()); }

    @Override
    public ResourceLocation getTextureLocation(CivCarEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
