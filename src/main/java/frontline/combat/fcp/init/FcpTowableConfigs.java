package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Trailers.TowableConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.mojang.logging.LogUtils;


import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * FcpTowableConfigs — loads hitch point configs for all towable vehicles.
 *
 * Reads from: data/<namespace>/towable_vehicles/<entity_id>.json
 * The entity_id must match the registry name of the vehicle entity exactly.
 * e.g. data/fcp/towable_vehicles/kamaz.json for the entity registered as "kamaz"
 *
 * Reloads automatically on /reload — no restart needed when tuning offsets.
 *
 * Usage:
 *   TowableConfig cfg = FcpTowableConfigs.get(ResourceLocation.fromNamespaceAndPath("fcp", "kamaz"));
 *   boolean towable   = FcpTowableConfigs.has(ResourceLocation.fromNamespaceAndPath("fcp", "kamaz"));
 */
@EventBusSubscriber(modid = FCP.MODID)
public class FcpTowableConfigs {

    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, TowableConfig> CONFIGS = new HashMap<>();

    /**
     * Returns the TowableConfig for the given entity registry id, or null if
     * no config exists for that vehicle. Null means the vehicle cannot tow.
     */
    public static TowableConfig get(ResourceLocation entityId) {
        return CONFIGS.get(entityId);
    }

    /** Returns true if this entity type has a towable config registered. */
    public static boolean has(ResourceLocation entityId) {
        return CONFIGS.containsKey(entityId);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Map<ResourceLocation, TowableConfig>>() {

            @Override
            protected Map<ResourceLocation, TowableConfig> prepare(
                    ResourceManager manager, ProfilerFiller profiler) {

                Map<ResourceLocation, TowableConfig> loaded = new HashMap<>();

                for (Map.Entry<ResourceLocation, Resource> entry :
                        manager.listResources("towable_vehicles",
                                path -> path != null && path.getPath().endsWith(".json")
                        ).entrySet()) {

                    ResourceLocation location = entry.getKey();
                    Resource resource = entry.getValue();

                    if (location == null || resource == null) continue;

                    String rawPath = location.getPath();
                    if (rawPath == null) continue;

                    try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                        JsonElement json = JsonParser.parseReader(reader);

                        TowableConfig config = TowableConfig.CODEC
                                .parse(JsonOps.INSTANCE, json)
                                .getOrThrow(err -> { LOGGER.error("[FCP] Bad towable config {}: {}", location, err); return new IllegalStateException(err); });

                        // Key is the entity registry id — strip folder prefix and extension
                        String stripped = rawPath
                                .replace("towable_vehicles/", "")
                                .replace(".json", "");

                        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(location.getNamespace(), stripped);
                        loaded.put(key, config);
                        LOGGER.debug("[FCP] Loaded towable config: {}", key);

                    } catch (Exception e) {
                        LOGGER.error("[FCP] Failed to load towable config {}: {}", location, e.getMessage());
                    }
                }

                return loaded;
            }

            @Override
            protected void apply(Map<ResourceLocation, TowableConfig> loaded,
                                 ResourceManager manager, ProfilerFiller profiler) {
                CONFIGS.clear();
                CONFIGS.putAll(loaded);
                LOGGER.info("[FCP] Loaded {} towable vehicle config(s)", CONFIGS.size());
            }
        });
    }
}
