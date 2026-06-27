package frontline.combat.fcp.client.renderer.Stryker;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import frontline.combat.fcp.client.model.Stryker.StrykerMGSModel;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerMGSEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class StrykerMGSRenderer extends VehicleRenderer<StrykerMGSEntity> {

    public StrykerMGSRenderer(EntityRendererProvider.Context renderManager) { super(renderManager, new StrykerMGSModel());}

    @Override
    public ResourceLocation getTextureLocation(StrykerMGSEntity entity) {
        return entity.getCurrentTexture();
    }
}
