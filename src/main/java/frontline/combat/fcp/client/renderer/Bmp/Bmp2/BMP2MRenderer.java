package frontline.combat.fcp.client.renderer.Bmp.Bmp2;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp.Bmp2.BMP2MModel;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp2.BMP2MEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class BMP2MRenderer extends VehicleRenderer<BMP2MEntity> {
    public BMP2MRenderer(EntityRendererProvider.Context c) { super(c, new BMP2MModel()); }
    @Override public ResourceLocation getTextureLocation(BMP2MEntity e) { return FcpVehicleTexture.resolve(e, e.getCurrentTexture()); }
}
