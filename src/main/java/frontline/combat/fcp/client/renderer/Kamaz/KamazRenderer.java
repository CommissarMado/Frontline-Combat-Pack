package frontline.combat.fcp.client.renderer.Kamaz;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Kamaz.KamazModel;
import frontline.combat.fcp.entity.vehicle.Kamaz.KamazEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class KamazRenderer extends VehicleRenderer<KamazEntity> {
    public KamazRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KamazModel());
    }

    @Override
    public ResourceLocation getTextureLocation(KamazEntity entity) {
        return entity.getCurrentTexture();
    }
}
