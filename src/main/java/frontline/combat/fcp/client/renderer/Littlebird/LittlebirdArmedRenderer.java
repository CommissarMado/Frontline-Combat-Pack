package frontline.combat.fcp.client.renderer.Littlebird;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Littlebird.LittlebirdArmedModel;
import frontline.combat.fcp.entity.vehicle.Littlebird.LittlebirdArmedEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class LittlebirdArmedRenderer extends VehicleRenderer<LittlebirdArmedEntity> {
    public LittlebirdArmedRenderer(EntityRendererProvider.Context renderManager) {super(renderManager, new LittlebirdArmedModel());}

    @Override
    public ResourceLocation getTextureLocation(LittlebirdArmedEntity entity) {
        return entity.getCurrentTexture();
    }
}
