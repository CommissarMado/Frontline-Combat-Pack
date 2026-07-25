package frontline.combat.fcp.vehicle.humvee;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared data + logic for the Humvee "Attachments" system.
 *
 * Each Humvee geo has an "Attachments" bone whose direct children are attachment
 * *categories* (Snorkels, FBumpers, RBumpers, Exhaust, Spare, Trunks, Tents...), and each
 * category's children are the mutually-exclusive *variants* (including empty variants that
 * act as "removed"). Exactly one variant per category is shown at a time.
 *
 * /fcp_humvee_attachments.json (generated from the geos) gives each category an axis-aligned
 * interaction box (vehicle-local, in blocks) sized to the union of all its variants' cubes.
 */
public final class HumveeAttachments {

    public static final class Category {
        public final String name;
        /** Vehicle-local AABB: [minX, minY, minZ, maxX, maxY, maxZ] in blocks. */
        public final double[] aabb;
        public final int variantCount;

        Category(String name, double[] aabb, int variantCount) {
            this.name = name;
            this.aabb = aabb;
            this.variantCount = variantCount;
        }

        /** The 8 corners of the local AABB. */
        public double[][] corners() {
            double[] a = aabb;
            return new double[][]{
                    {a[0], a[1], a[2]}, {a[0], a[1], a[5]}, {a[0], a[4], a[2]}, {a[0], a[4], a[5]},
                    {a[3], a[1], a[2]}, {a[3], a[1], a[5]}, {a[3], a[4], a[2]}, {a[3], a[4], a[5]}
            };
        }
    }

    private static final Map<String, List<Category>> DATA = new LinkedHashMap<>();
    private static boolean loaded = false;

    private HumveeAttachments() {}

    private static synchronized void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        try (InputStreamReader reader = new InputStreamReader(
                HumveeAttachments.class.getResourceAsStream("/fcp_humvee_attachments.json"),
                StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, com.google.gson.JsonElement> veh : root.entrySet()) {
                List<Category> cats = new ArrayList<>();
                JsonObject catObj = veh.getValue().getAsJsonObject();
                for (Map.Entry<String, com.google.gson.JsonElement> ce : catObj.entrySet()) {
                    JsonObject c = ce.getValue().getAsJsonObject();
                    JsonArray ja = c.getAsJsonArray("aabb");
                    double[] box = new double[6];
                    for (int i = 0; i < 6; i++) box[i] = ja.get(i).getAsDouble();
                    cats.add(new Category(ce.getKey(), box, c.get("variants").getAsInt()));
                }
                DATA.put(veh.getKey(), cats);
            }
        } catch (Exception e) {
            System.err.println("[FCP] Failed to load humvee attachment data: " + e);
        }
    }

    public static String vehicleName(net.minecraft.world.entity.EntityType<?> type) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        return id == null ? "" : id.getPath();
    }

    public static List<Category> categories(String vehicle) {
        ensureLoaded();
        return DATA.getOrDefault(vehicle, List.of());
    }

    /**
     * Show only the selected variant of each category, hiding every other variant's whole
     * sub-tree. Called every frame per instance (setHidden state is not per-instance on the
     * shared baked model and does not cascade, so it must be re-applied exhaustively).
     */
    public static void applyVisibility(GeoModel<?> model, HumveeVehicle vehicle) {
        model.getBone("Attachments").ifPresent(attachments -> {
            for (GeoBone category : attachments.getChildBones()) {
                int selected = vehicle.getAttachmentIndex(category.getName());
                List<GeoBone> variants = category.getChildBones();
                for (int i = 0; i < variants.size(); i++) {
                    setHiddenDeep(variants.get(i), i != selected);
                }
            }
        });
    }

    private static void setHiddenDeep(GeoBone bone, boolean hidden) {
        bone.setHidden(hidden);
        for (GeoBone child : bone.getChildBones()) {
            setHiddenDeep(child, hidden);
        }
    }
}
