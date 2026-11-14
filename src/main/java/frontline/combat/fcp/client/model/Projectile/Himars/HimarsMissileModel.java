package frontline.combat.fcp.client.model.Projectile.Himars;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.projectile.Himars.HimarsMissileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** The GMLRS in flight: its own model, on the HIMARS dark grey (TEX 4) texture. */
public class HimarsMissileModel extends GeoModel<HimarsMissileEntity> {

    @Override
    public ResourceLocation getAnimationResource(HimarsMissileEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("superbwarfare", "animations/javelin.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(HimarsMissileEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FCP.MODID, "geo/himars_missile.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HimarsMissileEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(FCP.MODID, "textures/entity/himars/dark_grey.png");
    }
}
