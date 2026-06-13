package frontline.combat.fcp.client.renderer.Lav;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.atsuishio.superbwarfare.tools.ParticleTool;
import com.mojang.blaze3d.vertex.PoseStack;
import frontline.combat.fcp.client.model.Bmp.BMP1Model;
import frontline.combat.fcp.client.model.Lav.Lav25Model;
import frontline.combat.fcp.entity.vehicle.Bmp.BMP1Entity;
import frontline.combat.fcp.entity.vehicle.Lav.Lav25Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class Lav25Renderer extends VehicleRenderer<Lav25Entity> {

    public Lav25Renderer(EntityRendererProvider.Context renderManager) { super(renderManager, new Lav25Model());}

    @Override
    public ResourceLocation getTextureLocation(Lav25Entity entity) {
        return entity.getCurrentTexture();
    }
}
