package frontline.combat.fcp.client.renderer.Ch53;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Ch53.Ch53eModel;
import frontline.combat.fcp.entity.vehicle.Ch53.Ch53eEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class Ch53eRenderer extends VehicleRenderer<Ch53eEntity> {

    public Ch53eRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Ch53eModel());
    }

    @Override
    public ResourceLocation getTextureLocation(Ch53eEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }
}
