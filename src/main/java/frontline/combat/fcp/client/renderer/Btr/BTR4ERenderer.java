package frontline.combat.fcp.client.renderer.Btr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Btr.BTR4EModel;
import frontline.combat.fcp.entity.vehicle.Btr.BTR4EEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class BTR4ERenderer extends VehicleRenderer<BTR4EEntity> {
    public BTR4ERenderer(EntityRendererProvider.Context c) { super(c, new BTR4EModel()); }
    @Override public ResourceLocation getTextureLocation(BTR4EEntity e) { return FcpVehicleTexture.resolve(e, e.getCurrentTexture()); }
}
