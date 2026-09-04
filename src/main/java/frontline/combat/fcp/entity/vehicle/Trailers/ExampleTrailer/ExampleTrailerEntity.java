package frontline.combat.fcp.entity.vehicle.Trailers.ExampleTrailer;

import frontline.combat.fcp.entity.vehicle.Trailers.AbstractTrailerEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * ExampleTrailerEntity — minimal concrete trailer. Copy this as a template.
 *
 * A new trailer needs only THREE things beyond this class:
 *   1. Registration in ModEntities + ModEntityRenderers
 *   2. Its SBW vehicle data:  data/<ns>/sbw/vehicles/<id>.json  (EngineType "Empty")
 *   3. Its tow config:        data/<ns>/trailer_towed/<id>.json  (tongue + whitelist)
 * Plus a trailer_driver JSON on every vehicle that should be able to tow it.
 *
 * Everything about hitching lives in JSON, resolved by registry id — no per-trailer
 * Java needed for the tongue point, whitelist, or articulation.
 *
 * Animation note: the controller returns PlayState.STOP. Only call
 * state.setAndContinue(RawAnimation) once a matching animation JSON actually
 * exists, or GeckoLib will crash on a null ResourceLocation.
 *
 * Geo model:  assets/fcp/geo/lav25.geo.json        (placeholder — swap for your own)
 * Textures:   assets/fcp/textures/entity/lav/...    (placeholder camo set)
 */
public class ExampleTrailerEntity extends AbstractTrailerEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ExampleTrailerEntity(EntityType<ExampleTrailerEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar reg) {
        reg.add(new AnimationController<>(this, "base", 0, state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}