package frontline.combat.fcp.client.model.Projectile.Hellfire;

import com.atsuishio.superbwarfare.Mod;
import frontline.combat.fcp.entity.projectile.Hellfire.WireGuidedHellfireEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WireGuidedHellfireModel extends GeoModel<WireGuidedHellfireEntity> {

    @Override
    public ResourceLocation getAnimationResource(WireGuidedHellfireEntity entity) {
        return Mod.loc("animations/javelin_missile.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(WireGuidedHellfireEntity entity) {
        // Используем модель igla как базу (можно заменить на свою)
        return Mod.loc("geo/hellfire.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WireGuidedHellfireEntity entity) {
        return Mod.loc("textures/entity/hellfire/hellfire.png");
    }
}
