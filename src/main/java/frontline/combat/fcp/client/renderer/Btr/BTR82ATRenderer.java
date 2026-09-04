package frontline.combat.fcp.client.renderer.Btr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Btr.BTR82ATModel;
import frontline.combat.fcp.entity.vehicle.Btr.BTR82ATEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class BTR82ATRenderer extends VehicleRenderer<BTR82ATEntity> {
    public BTR82ATRenderer(EntityRendererProvider.Context c) { super(c, new BTR82ATModel()); }
    @Override public ResourceLocation getTextureLocation(BTR82ATEntity e) { return FcpVehicleTexture.resolve(e, e.getCurrentTexture()); }
}
