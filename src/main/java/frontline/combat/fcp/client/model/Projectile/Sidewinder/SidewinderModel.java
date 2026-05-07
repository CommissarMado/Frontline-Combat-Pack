package frontline.combat.fcp.client.model.Projectile.Sidewinder;

import com.atsuishio.superbwarfare.Mod;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.projectile.Hellfire.LockOnHellfireEntity;
import frontline.combat.fcp.entity.projectile.Sidewinder.SidewinderEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SidewinderModel extends GeoModel<SidewinderEntity> {

    @Override
    public ResourceLocation getAnimationResource(SidewinderEntity entity) {
        return Mod.loc("animations/javelin_missile.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(SidewinderEntity entity) {
        return new ResourceLocation(FCP.MODID, "geo/sidewinder.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SidewinderEntity entity) {
        return new ResourceLocation(FCP.MODID,"textures/entity/sidewinder/sidewinder.png");
    }
}
