package frontline.combat.fcp.client.renderer.Bmp.Bmp2;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp.Bmp2.BMP2MDModel;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp2.BMP2MDEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class BMP2MDRenderer extends VehicleRenderer<BMP2MDEntity> {
    public BMP2MDRenderer(EntityRendererProvider.Context c) { super(c, new BMP2MDModel()); }
    @Override public ResourceLocation getTextureLocation(BMP2MDEntity e) { return FcpVehicleTexture.resolve(e, e.getCurrentTexture()); }
}
