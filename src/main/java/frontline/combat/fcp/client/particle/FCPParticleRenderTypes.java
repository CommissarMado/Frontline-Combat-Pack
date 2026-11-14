package frontline.combat.fcp.client.particle;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import frontline.combat.fcp.FCP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Vanilla's translucent particles write depth, so whatever is drawn after them (clouds, other particles) vanishes behind even
 * their faintest pixels. These don't. SOFT also uses a shader without vanilla's cut-off at 10% opacity, which big faint puffs
 * fall below almost entirely, and which fades them out as they near the ground instead of being cut off by it in a line.
 */
@EventBusSubscriber(modid = FCP.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FCPParticleRenderTypes {

    private static ShaderInstance softShader;
    private static RenderTarget depthCopy;

    /** IrisApi.isShaderPackInUse, looked up by name because Iris and Oculus are optional. */
    private static Object irisApi;
    private static Method shaderPackInUse;
    private static boolean irisLookedUp;

    private static final ParticleRenderType NO_DEPTH_WRITE = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textures) {
            return setUp(tesselator);
        }

        @Override
        public String toString() {
            return "FCP_NO_DEPTH_WRITE";
        }
    };

    public static final ParticleRenderType SOFT = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textures) {
            copySceneDepth();
            BufferBuilder builder = setUp(tesselator);
            RenderSystem.setShaderTexture(1, depthCopy.getDepthTextureId());
            RenderSystem.setShader(() -> softShader);
            return builder;
        }

        @Override
        public String toString() {
            return "FCP_SOFT";
        }
    };

    /** A target can't be read while it is being drawn to. Only runs on frames with soft particles to draw. */
    private static void copySceneDepth() {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget particlesTarget = mc.levelRenderer.getParticlesTarget();
        RenderTarget scene = particlesTarget != null ? particlesTarget : mc.getMainRenderTarget();
        if (depthCopy == null) depthCopy = new TextureTarget(scene.width, scene.height, true, Minecraft.ON_OSX);
        else if (depthCopy.width != scene.width || depthCopy.height != scene.height) depthCopy.resize(scene.width, scene.height, Minecraft.ON_OSX);
        depthCopy.copyDepthFrom(scene);
        scene.bindWrite(true);
    }

    private FCPParticleRenderTypes() {
    }

    public static ParticleRenderType noDepthWrite() {
        return shaderPackInUse() ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : NO_DEPTH_WRITE;
    }

    /** With a shader pack, Iris and Oculus only draw vanilla's render types: anything else silently never shows. */
    public static boolean shaderPackInUse() {
        if (!irisLookedUp) {
            irisLookedUp = true;
            try {
                Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                irisApi = api.getMethod("getInstance").invoke(null);
                shaderPackInUse = api.getMethod("isShaderPackInUse");
            } catch (ReflectiveOperationException | LinkageError e) {
                shaderPackInUse = null;
            }
        }
        if (shaderPackInUse == null) return false;
        try {
            return (boolean) shaderPackInUse.invoke(irisApi);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static BufferBuilder setUp(Tesselator tesselator) {
        RenderSystem.depthMask(false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(FCP.MODID, "soft_particle"),
                DefaultVertexFormat.PARTICLE), shader -> softShader = shader);
    }
}
