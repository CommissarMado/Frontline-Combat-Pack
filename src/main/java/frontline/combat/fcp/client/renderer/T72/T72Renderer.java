package frontline.combat.fcp.client.renderer.T72;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.model.T72.T72Model;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;
import frontline.combat.fcp.entity.vehicle.T72.T72Entity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class T72Renderer extends VehicleRenderer<T72Entity> {

    public T72Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new T72Model());
        addRenderLayer(new ShtoraLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(T72Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }

    /**
     * The T-90A's Shtora dazzler: while the gunner has the "Shtora" weapon selected, the
     * lamps ("fonart" bone) glow red, fading in over {@link #FADE_IN} seconds (and out over {@link #FADE_OUT}). The mask
     * texture is transparent except those cubes' pixels (all but the dark centre of each front face), so
     * redrawing the model with it at full brightness lights only the emitters. It's drawn as an opaque-ish
     * emissive overlay rather than additively, so the red isn't washed pink by the paint underneath.
     */
    private static final class ShtoraLayer extends GeoRenderLayer<T72Entity> {

        private static final ResourceLocation MASK = new ResourceLocation(FCP.MODID, "textures/entity/t72/t90a_shtora_glow.png");
        private static final float FADE_IN = 0.8f, FADE_OUT = 0.4f, MAX_OPACITY = 1.0f;

        /** Glow level per vehicle (0..1) and when it was last updated (nanoTime); client only. */
        private final java.util.Map<T72Entity, float[]> levels = new java.util.WeakHashMap<>();
        private final java.util.Map<T72Entity, Long> updated = new java.util.WeakHashMap<>();

        ShtoraLayer(VehicleRenderer<T72Entity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, T72Entity vehicle, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource,
                           VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            if (!"t90a".equals(vehicle.variant())) return;
            boolean on = !vehicle.isWreck() && "Shtora".equals(vehicle.getGunName(vehicle.computed().getTurretControllerIndex()));

            long now = System.nanoTime();
            float[] level = levels.computeIfAbsent(vehicle, v -> new float[1]);
            Long last = updated.put(vehicle, now);
            float dt = last == null ? 0f : Math.min((now - last) / 1.0e9f, 0.25f);
            level[0] = on ? Math.min(1f, level[0] + dt / FADE_IN) : Math.max(0f, level[0] - dt / FADE_OUT);
            if (level[0] <= 0f) return;

            // Ease in so the glow swells rather than snapping on; never fully opaque, so a little of the
            // emitter's own texture shows through and it reads as light, not solid plastic.
            float a = MAX_OPACITY * level[0] * level[0] * (3f - 2f * level[0]);
            RenderType glow = RenderType.entityTranslucentEmissive(MASK);
            getRenderer().reRender(model, poseStack, bufferSource, vehicle, glow, bufferSource.getBuffer(glow), partialTick,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, a);
        }
    }
}
