package frontline.combat.fcp.client.renderer.Btr82Cope;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Btr82Cope.BTR82CopeModel;
import frontline.combat.fcp.entity.vehicle.Btr82Cope.BTR82CopeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BTR82CopeRenderer extends VehicleRenderer<BTR82CopeEntity> {
    public BTR82CopeRenderer(EntityRendererProvider.Context c) { super(c, new BTR82CopeModel()); }
    @Override public ResourceLocation getTextureLocation(BTR82CopeEntity e) { return e.getCurrentTexture(); }
}
