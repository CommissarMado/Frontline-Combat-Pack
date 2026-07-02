package frontline.combat.fcp.client.renderer.Fmtv;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Fmtv.FMTVModel;
import frontline.combat.fcp.client.model.Ural.UralModel;
import frontline.combat.fcp.entity.vehicle.Fmtv.FMTVEntity;
import frontline.combat.fcp.entity.vehicle.Ural.UralEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class FMTVRenderer extends VehicleRenderer<FMTVEntity> {
    public FMTVRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FMTVModel());
    }

    @Override
    public ResourceLocation getTextureLocation(FMTVEntity entity) {
        return entity.getCurrentTexture();
    }
}
