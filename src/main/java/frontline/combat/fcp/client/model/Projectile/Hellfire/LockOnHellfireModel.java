package frontline.combat.fcp.client.model.Projectile.Hellfire;

import com.atsuishio.superbwarfare.Mod;
import frontline.combat.fcp.FCP;
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
        return new ResourceLocation(FCP.MODID, "geo/hellfire.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LockOnHellfireEntity entity) {
        return new ResourceLocation(FCP.MODID, "textures/entity/hellfire/hellfire.png");
    }
}
