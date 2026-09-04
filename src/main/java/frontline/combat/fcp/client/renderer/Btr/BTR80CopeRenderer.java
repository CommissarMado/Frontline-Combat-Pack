package frontline.combat.fcp.client.renderer.Btr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Btr.BTR80CopeModel;
import frontline.combat.fcp.entity.vehicle.Btr.BTR80CopeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class BTR80CopeRenderer extends VehicleRenderer<BTR80CopeEntity> {
    public BTR80CopeRenderer(EntityRendererProvider.Context c) { super(c, new BTR80CopeModel()); }
    @Override public ResourceLocation getTextureLocation(BTR80CopeEntity e) { return FcpVehicleTexture.resolve(e, e.getCurrentTexture()); }
}
