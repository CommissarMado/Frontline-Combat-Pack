package frontline.combat.fcp.client.renderer.Stryker;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Stryker.StrykerM2Model;
import frontline.combat.fcp.client.model.Stryker.StrykerMGSModel;
import frontline.combat.fcp.client.renderer.FCPVehicleRenderer;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerM2Entity;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerMGSEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class StrykerM2Renderer extends FCPVehicleRenderer<StrykerM2Entity> {

    public StrykerM2Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new StrykerM2Model());}

    @Override
    protected float tiltStrength() {
        return 0.25f; // (0 = flat, 1 = SBW default)
    }

    @Override
    public ResourceLocation getTextureLocation(StrykerM2Entity entity) {
        return entity.getCurrentTexture();
    }
}
