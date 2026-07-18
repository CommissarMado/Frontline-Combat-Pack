package frontline.combat.fcp.client.renderer.Stryker;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Stryker.StrykerTowModel;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerTowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class StrykerTowRenderer extends VehicleRenderer<StrykerTowEntity> {
    public StrykerTowRenderer(EntityRendererProvider.Context c) { super(c, new StrykerTowModel()); }
    @Override public ResourceLocation getTextureLocation(StrykerTowEntity e) { return e.getCurrentTexture(); }
}
