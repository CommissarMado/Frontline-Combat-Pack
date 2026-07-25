package frontline.combat.fcp.client.renderer.Stryker;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Stryker.StrykerMortarModel;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerMortarEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class StrykerMortarRenderer extends VehicleRenderer<StrykerMortarEntity> {
    public StrykerMortarRenderer(EntityRendererProvider.Context c) { super(c, new StrykerMortarModel()); }
    @Override public ResourceLocation getTextureLocation(StrykerMortarEntity e) { return e.getCurrentTexture(); }
}
