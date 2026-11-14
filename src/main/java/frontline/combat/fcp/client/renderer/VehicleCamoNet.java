package frontline.combat.fcp.client.renderer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.vehicle.CamoNetVehicle;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.RenderUtil;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * The camo net (fcp:camo_net, CamoNetVehicle) on the Abrams (light: Squad M1A2 layout, heavy: after
 * War Thunder M1A2C) and the Leopard 2 (light and heavy) - draped sheets of quads on the turret, hull and gun tube, textured with a tiled 16 px net in one of five colours (jungle, woodland, olive, timber, desert).
 * <p>
 * The sheets are precomputed per geo by the NetDrape and LeoDrape tools into assets/fcp/camo_net/&lt;geo&gt;.json (light) and
 * &lt;geo&gt;_heavy.json, as {"&lt;bone&gt;": [x y z u v] x 4 + normal per quad} in the bone's render frame (GeckoLib's
 * baked space, blocks), for the hull, "turret" and gun tube bones of each tank (the tube recoils). While the model renders, the renderer hands each bone to {@link #capture},
 * which keeps that bone's matrix (GeckoLib's own prepMatrixForBone, so the net turns and tilts with it); after the model
 * {@link #draw} puts every quad into a single buffer. Back faces are culled (the quads wind counter-clockwise seen
 * from outside), so a sheet seen from behind costs no fragments. No extra model, bones or animation work.
 */
public final class VehicleCamoNet {

    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    /** Net texture per colour (CamoNetVehicle.getCamoNetColor). */
    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(FCP.MODID, "textures/entity/abrams/camo_net_jungle.png"),
            ResourceLocation.fromNamespaceAndPath(FCP.MODID, "textures/entity/abrams/camo_net_woodland.png"),
            ResourceLocation.fromNamespaceAndPath(FCP.MODID, "textures/entity/abrams/camo_net_olive.png"),
            ResourceLocation.fromNamespaceAndPath(FCP.MODID, "textures/entity/abrams/camo_net_timber.png"),
            ResourceLocation.fromNamespaceAndPath(FCP.MODID, "textures/entity/abrams/camo_net_desert.png")};
    /** Mesh file suffix per preset: 1 light (Squad M1A2 layout), 2 heavy (War Thunder M1A2C layout). */
    private static final String[] SUFFIX = {null, "", "_heavy"};
    /** Floats per quad: 4 vertices x (x y z u v) + normal. */
    private static final int QUAD = 23;
    /** Net meshes by geo name; an empty map for a geo without one. */
    private static final Map<String, Map<String, float[]>> MESHES = new HashMap<>();

    private Map<String, float[]> mesh;
    private final Map<String, Frame> poses = new HashMap<>();

    /** A captured bone frame. */
    private record Frame(Matrix4f pose, Matrix3f normal) {
    }

    /** Before the model renders: picks the vehicle's net, if it has one fitted. */
    public <T extends VehicleEntity & CamoNetVehicle> void begin(T vehicle) {
        poses.clear();
        mesh = !vehicle.isWreck() && vehicle.getCamoNet() > 0 && vehicle.getCamoNet() < SUFFIX.length ? mesh(vehicle.camoNetName() + SUFFIX[vehicle.getCamoNet()]) : null;
        if (mesh != null && mesh.isEmpty()) mesh = null;
    }

    /** During the model render, with the bone's parent frame on the stack: keeps the bone's own frame for its net quads. */
    public void capture(PoseStack poseStack, GeoBone bone) {
        if (mesh == null || bone.isHidden() || !mesh.containsKey(bone.getName())) return;
        poseStack.pushPose();
        RenderUtil.prepMatrixForBone(poseStack, bone);
        PoseStack.Pose last = poseStack.last();
        poses.put(bone.getName(), new Frame(new Matrix4f(last.pose()), new Matrix3f(last.normal())));
        poseStack.popPose();
    }

    /** After the model: the net quads of every captured bone. */
    public void draw(CamoNetVehicle vehicle, MultiBufferSource bufferSource, int packedLight) {
        if (mesh == null || poses.isEmpty()) return;
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURES[Math.floorMod(vehicle.getCamoNetColor(), TEXTURES.length)]));
        for (Map.Entry<String, Frame> e : poses.entrySet()) {
            float[] q = mesh.get(e.getKey());
            Matrix4f pose = e.getValue().pose();
            Matrix3f normal = e.getValue().normal();
            for (int i = 0; i + QUAD <= q.length; i += QUAD) {
                Vector3f n = normal.transform(q[i + 20], q[i + 21], q[i + 22], new Vector3f());
                for (int v = 0; v < 4; v++) {
                    int o = i + v * 5;
                    buffer.addVertex(pose, q[o], q[o + 1], q[o + 2]).setColor(1f, 1f, 1f, 1f).setUv(q[o + 3], q[o + 4])
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(n.x(), n.y(), n.z());
                }
            }
        }
        poses.clear();
    }

    private static Map<String, float[]> mesh(String geo) {
        return MESHES.computeIfAbsent(geo, VehicleCamoNet::load);
    }

    private static Map<String, float[]> load(String geo) {
        Map<String, float[]> out = new HashMap<>();
        ResourceLocation file = ResourceLocation.fromNamespaceAndPath(FCP.MODID, "camo_net/" + geo + ".json");
        var resource = Minecraft.getInstance().getResourceManager().getResource(file);
        if (resource.isEmpty()) return out;
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> e : json.entrySet()) {
                var arr = e.getValue().getAsJsonArray();
                float[] q = new float[arr.size()];
                for (int i = 0; i < q.length; i++) q[i] = arr.get(i).getAsFloat();
                out.put(e.getKey(), q);
            }
        } catch (Exception ex) {
            LOGGER.warn("Couldn't read camo net {}: {}", file, ex.toString());
        }
        return out;
    }
}
