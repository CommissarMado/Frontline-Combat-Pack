package frontline.combat.fcp.client.renderer.Toyota;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Toyota.ToyotaHiluxMortarModel;
import frontline.combat.fcp.entity.vehicle.Toyota.ToyotaHiluxMortarEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ToyotaHiluxMortarRenderer extends VehicleRenderer<ToyotaHiluxMortarEntity> {
    public ToyotaHiluxMortarRenderer(EntityRendererProvider.Context c) { super(c, new ToyotaHiluxMortarModel()); }
    @Override public ResourceLocation getTextureLocation(ToyotaHiluxMortarEntity e) { return e.getCurrentTexture(); }
}
