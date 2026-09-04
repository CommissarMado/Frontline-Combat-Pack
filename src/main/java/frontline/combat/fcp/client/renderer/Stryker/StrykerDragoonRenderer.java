package frontline.combat.fcp.client.renderer.Stryker;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Stryker.StrykerDragoonModel;
import frontline.combat.fcp.entity.vehicle.Stryker.StrykerDragoonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class StrykerDragoonRenderer extends VehicleRenderer<StrykerDragoonEntity> {
    public StrykerDragoonRenderer(EntityRendererProvider.Context c) { super(c, new StrykerDragoonModel()); }
    @Override public ResourceLocation getTextureLocation(StrykerDragoonEntity e) { return FcpVehicleTexture.resolve(e, e.getCurrentTexture()); }
}
