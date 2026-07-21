package frontline.combat.fcp.client.renderer.Bmp1p;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp1p.BMP1PModel;
import frontline.combat.fcp.entity.vehicle.Bmp1p.BMP1PEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BMP1PRenderer extends VehicleRenderer<BMP1PEntity> {
    public BMP1PRenderer(EntityRendererProvider.Context c) { super(c, new BMP1PModel()); }
    @Override public ResourceLocation getTextureLocation(BMP1PEntity e) { return e.getCurrentTexture(); }
}
