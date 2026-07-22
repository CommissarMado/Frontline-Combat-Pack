package frontline.combat.fcp.client.renderer.Btr4mv1;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Btr4mv1.BTR4MV1Model;
import frontline.combat.fcp.entity.vehicle.Btr4mv1.BTR4MV1Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BTR4MV1Renderer extends VehicleRenderer<BTR4MV1Entity> {
    public BTR4MV1Renderer(EntityRendererProvider.Context c) { super(c, new BTR4MV1Model()); }
    @Override public ResourceLocation getTextureLocation(BTR4MV1Entity e) { return e.getCurrentTexture(); }
}
