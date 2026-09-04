package frontline.combat.fcp.client.renderer.Bmp.Bmp2;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Bmp.Bmp2.BMP2NoAtgmModel;
import frontline.combat.fcp.entity.vehicle.Bmp.Bmp2.BMP2NoAtgmEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class BMP2NoAtgmRenderer extends VehicleRenderer<BMP2NoAtgmEntity> {
    public BMP2NoAtgmRenderer(EntityRendererProvider.Context c) { super(c, new BMP2NoAtgmModel()); }
    @Override public ResourceLocation getTextureLocation(BMP2NoAtgmEntity e) { return FcpVehicleTexture.resolve(e, e.getCurrentTexture()); }
}
