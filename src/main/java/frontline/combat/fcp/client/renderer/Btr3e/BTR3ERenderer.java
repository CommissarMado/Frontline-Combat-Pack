package frontline.combat.fcp.client.renderer.Btr3e;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Btr3e.BTR3EModel;
import frontline.combat.fcp.entity.vehicle.Btr3e.BTR3EEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BTR3ERenderer extends VehicleRenderer<BTR3EEntity> {
    public BTR3ERenderer(EntityRendererProvider.Context c) { super(c, new BTR3EModel()); }
    @Override public ResourceLocation getTextureLocation(BTR3EEntity e) { return e.getCurrentTexture(); }
}
