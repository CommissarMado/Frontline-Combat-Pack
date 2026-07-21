package frontline.combat.fcp.client.renderer.Bmp2d;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp2d.BMP2DModel;
import frontline.combat.fcp.entity.vehicle.Bmp2d.BMP2DEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BMP2DRenderer extends VehicleRenderer<BMP2DEntity> {
    public BMP2DRenderer(EntityRendererProvider.Context c) { super(c, new BMP2DModel()); }
    @Override public ResourceLocation getTextureLocation(BMP2DEntity e) { return e.getCurrentTexture(); }
}
