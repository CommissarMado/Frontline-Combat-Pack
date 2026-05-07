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
 * Animation note: the controller returns PlayState.STOP when no animation
 * is playing. This is intentional — do NOT call state.setAndContinue() with
 * a RawAnimation unless the animation JSON file exists at the path returned
 * by your model's getAnimationResource(). GeckoLib will crash with a null
 * ResourceLocation if the file is missing or the animation name doesn't match.
 *
 * To add animations:
 *   1. Create the animation JSON at:
 *      assets/fcp/animations/vehicle/trailer/example_trailer.animation.json
 *   2. Add your animation name inside that file (e.g. "animation.example_trailer.idle")
 *   3. Then uncomment the RawAnimation block below and use setAndContinue()
 *
 * JSON config: data/fcp/trailers/example_trailer.json
 * Geo model:   assets/fcp/geo/vehicle/trailer/example_trailer.geo.json
 * Texture:     assets/fcp/textures/entity/vehicle/trailer/example_trailer.png
 * Animations:  assets/fcp/animations/vehicle/trailer/example_trailer.animation.json
 */
public class ExampleTrailerEntity extends AbstractTrailerEntity {

    private static final ResourceLocation CONFIG_ID =
            new ResourceLocation("fcp", "example_trailer");

    private static final ResourceLocation[] CAMO_TEXTURES = {
            new ResourceLocation("fcp", "textures/entity/lav/lav25_camo1.png"),
            new ResourceLocation("fcp", "textures/entity/lav/lav25_camo2.png"),
            new ResourceLocation("fcp", "textures/entity/lav/lav25_camo3.png"),
            new ResourceLocation("fcp", "textures/entity/lav/lav25_od.png"),
            new ResourceLocation("fcp", "textures/entity/lav/lav25_tan.png")
    };

    private static final String[] CAMO_NAMES = {"Camo Variant 1","Camo Variant 2", "Camo Variant 3","No-Camo", "Tan"};

    // Uncomment once your animation JSON exists and contains this animation name:
    // private static final RawAnimation IDLE =
    //         RawAnimation.begin().thenLoop("animation.example_trailer.idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ExampleTrailerEntity(EntityType<ExampleTrailerEntity> type, Level world) {
        super(type, world);
    }

    // ── AbstractTrailerEntity contract ───────────────────────────────────────

    @Override
    protected ResourceLocation getConfigId() {
        return CONFIG_ID;
    }

    // ── CamoVehicleBase contract ─────────────────────────────────────────────

    @Override
    public ResourceLocation[] getCamoTextures() {
        return CAMO_TEXTURES;
    }

    @Override
    public String[] getCamoNames() {
        return CAMO_NAMES;
    }

    // ── GeckoLib contract ────────────────────────────────────────────────────

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar reg) {
        reg.add(new AnimationController<>(this, "base", 0, state -> {
            // Return STOP until you have an animation file set up.
            // Calling state.setAndContinue() with a missing animation JSON
            // causes a GeckoLib null crash on the render thread.
            return PlayState.STOP;

            // Once your animation file exists, replace the above with:
            // return state.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}