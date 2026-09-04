package frontline.combat.fcp.client.renderer.Btr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Btr.BTR82CopeModel;
import frontline.combat.fcp.entity.vehicle.Btr.BTR82CopeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class BTR82CopeRenderer extends VehicleRenderer<BTR82CopeEntity> {
    public BTR82CopeRenderer(EntityRendererProvider.Context c) { super(c, new BTR82CopeModel()); }
    @Override public ResourceLocation getTextureLocation(BTR82CopeEntity e) { return FcpVehicleTexture.resolve(e, e.getCurrentTexture()); }
}
