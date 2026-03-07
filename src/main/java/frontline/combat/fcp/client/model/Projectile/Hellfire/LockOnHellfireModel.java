package frontline.combat.fcp.client.model.Projectile.Hellfire;

import com.atsuishio.superbwarfare.Mod;
import frontline.combat.fcp.entity.projectile.Hellfire.LockOnHellfireEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LockOnHellfireModel extends GeoModel<LockOnHellfireEntity> {

    @Override
    public ResourceLocation getAnimationResource(LockOnHellfireEntity entity) {
        return Mod.loc("animations/javelin_missile.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(LockOnHellfireEntity entity) {
        // Используем модель igla как базу (можно заменить на свою)
        return Mod.loc("geo/hellfire.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LockOnHellfireEntity entity) {
        return Mod.loc("textures/entity/hellfire/hellfire.png");
    }
}
