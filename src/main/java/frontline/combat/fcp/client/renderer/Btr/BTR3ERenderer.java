package frontline.combat.fcp.client.renderer.Btr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Btr.BTR3EModel;
import frontline.combat.fcp.entity.vehicle.Btr.BTR3EEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class BTR3ERenderer extends VehicleRenderer<BTR3EEntity> {
    public BTR3ERenderer(EntityRendererProvider.Context c) { super(c, new BTR3EModel()); }
    @Override public ResourceLocation getTextureLocation(BTR3EEntity e) { return FcpVehicleTexture.resolve(e, e.getCurrentTexture()); }
}
