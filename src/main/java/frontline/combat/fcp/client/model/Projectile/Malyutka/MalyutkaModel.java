package frontline.combat.fcp.client.model.Projectile.Malyutka;

import com.atsuishio.superbwarfare.Mod;
import frontline.combat.fcp.entity.projectile.Malyutka.MalyutkaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MalyutkaModel extends GeoModel<MalyutkaEntity> {

    @Override
    public ResourceLocation getAnimationResource(MalyutkaEntity entity) {
        return Mod.loc("animations/javelin_missile.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(MalyutkaEntity entity) {
        // Используем модель igla как базу (можно заменить на свою)
        return Mod.loc("geo/malyutka.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MalyutkaEntity entity) {
        return Mod.loc("textures/entity/malyutka/malyutka.png");
    }
}
