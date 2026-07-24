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
 * The category list + per-category interaction hitbox is generated from the geos into
 * /fcp_humvee_attachments.json (see the build tooling). Visibility itself is applied by
 * walking the live GeckoLib bone tree, so it stays correct even if a geo changes.
 */
public final class HumveeAttachments {

    public static final class Category {
        public final String name;
        public final float[] hitbox;   // vehicle-local offset (blocks): [x, y, z]
        public final int variantCount;

        Category(String name, float[] hitbox, int variantCount) {
            this.name = name;
            this.hitbox = hitbox;
            this.variantCount = variantCount;
        }
    }

    // vehicleName -> ordered categories
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
                    JsonArray hb = c.getAsJsonArray("hitbox");
                    float[] hit = {hb.get(0).getAsFloat(), hb.get(1).getAsFloat(), hb.get(2).getAsFloat()};
                    int count = c.getAsJsonArray("variants").size();
                    cats.add(new Category(ce.getKey(), hit, count));
                }
                DATA.put(veh.getKey(), cats);
            }
        } catch (Exception e) {
            // Missing/broken data just means no attachments; never crash the vehicle.
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
