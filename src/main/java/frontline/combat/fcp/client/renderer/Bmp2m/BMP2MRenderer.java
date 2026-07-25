package frontline.combat.fcp.client.renderer.Bmp2m;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp2m.BMP2MModel;
import frontline.combat.fcp.entity.vehicle.Bmp2m.BMP2MEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BMP2MRenderer extends VehicleRenderer<BMP2MEntity> {
    public BMP2MRenderer(EntityRendererProvider.Context c) { super(c, new BMP2MModel()); }
    @Override public ResourceLocation getTextureLocation(BMP2MEntity e) { return e.getCurrentTexture(); }
}
