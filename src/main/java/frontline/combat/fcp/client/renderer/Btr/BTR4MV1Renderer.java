package frontline.combat.fcp.client.renderer.Btr;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Btr.BTR4MV1Model;
import frontline.combat.fcp.entity.vehicle.Btr.BTR4MV1Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BTR4MV1Renderer extends VehicleRenderer<BTR4MV1Entity> {
    public BTR4MV1Renderer(EntityRendererProvider.Context c) { super(c, new BTR4MV1Model()); }
    @Override public ResourceLocation getTextureLocation(BTR4MV1Entity e) { return e.getCurrentTexture(); }
}
