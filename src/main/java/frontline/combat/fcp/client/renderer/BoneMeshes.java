package frontline.combat.fcp.client.renderer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import software.bernie.geckolib.cache.object.GeoBone;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Blockbench "mesh" elements for GeckoLib vehicles. geo.json can only hold cubes, so a model's
 * meshes are converted to a sidecar file (assets/fcp/meshes/name.json) keyed by bone name, and the
 * renderer draws them from renderCubesOfBone - in the bone's pose, so they follow rotors, doors and
 * hidden bones exactly like the bone's cubes.
 * <p>
 * File format: {"bones": {"bone": [[x y z u v] x4, nx ny nz], ...}} - 23 numbers per face, positions
 * in GeckoLib model space (Blockbench pixels / 16), uv already divided by the texture size.
 * Triangles repeat their last vertex.
 */
public final class BoneMeshes {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, Map<String, float[][]>> CACHE = new ConcurrentHashMap<>();

    private BoneMeshes() {
    }

    public static Map<String, float[][]> get(ResourceLocation location) {
        return CACHE.computeIfAbsent(location, BoneMeshes::load);
    }

    private static Map<String, float[][]> load(ResourceLocation location) {
        Map<String, float[][]> bones = new HashMap<>();
        try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(location)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("bones");
            for (Map.Entry<String, JsonElement> bone : root.entrySet()) {
                JsonArray faces = bone.getValue().getAsJsonArray();
                float[][] out = new float[faces.size()][];
                for (int i = 0; i < out.length; i++) {
                    JsonArray face = faces.get(i).getAsJsonArray();
                    float[] f = new float[23];
                    for (int k = 0; k < 23; k++) f[k] = face.get(k).getAsFloat();
                    out[i] = f;
                }
                bones.put(bone.getKey(), out);
            }
        } catch (Exception e) {
            LOGGER.error("Could not load bone meshes {}", location, e);
        }
        return bones;
    }

    /** Emits the bone's mesh faces with the current (bone-posed) matrix, the way GeckoLib emits cube quads. */
    public static void render(Map<String, float[][]> meshes, GeoBone bone, PoseStack poseStack, VertexConsumer buffer,
                              int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        float[][] faces = meshes.get(bone.getName());
        if (faces == null) return;
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normalPose = poseStack.last().normal();
        Vector4f pos = new Vector4f();
        Vector3f normal = new Vector3f();
        for (float[] f : faces) {
            normalPose.transform(normal.set(f[20], f[21], f[22]));
            for (int i = 0; i < 4; i++) {
                int o = i * 5;
                pose.transform(pos.set(f[o], f[o + 1], f[o + 2], 1f));
                buffer.vertex(pos.x(), pos.y(), pos.z(), red, green, blue, alpha, f[o + 3], f[o + 4],
                        packedOverlay, packedLight, normal.x(), normal.y(), normal.z());
            }
        }
    }
}
