package frontline.combat.fcp.client.renderer.Btr80Cope;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Btr80Cope.BTR80CopeModel;
import frontline.combat.fcp.entity.vehicle.Btr80Cope.BTR80CopeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BTR80CopeRenderer extends VehicleRenderer<BTR80CopeEntity> {
    public BTR80CopeRenderer(EntityRendererProvider.Context c) { super(c, new BTR80CopeModel()); }
    @Override public ResourceLocation getTextureLocation(BTR80CopeEntity e) { return e.getCurrentTexture(); }
}
