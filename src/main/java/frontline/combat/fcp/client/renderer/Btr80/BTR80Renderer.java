package frontline.combat.fcp.client.renderer.Btr80;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import frontline.combat.fcp.client.model.Btr80.BTR80Model;
import frontline.combat.fcp.entity.vehicle.Btr80.BTR80Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BTR80Renderer extends VehicleRenderer<BTR80Entity> {
    public BTR80Renderer(EntityRendererProvider.Context c) { super(c, new BTR80Model()); }
    @Override public ResourceLocation getTextureLocation(BTR80Entity e) { return e.getCurrentTexture(); }
}
