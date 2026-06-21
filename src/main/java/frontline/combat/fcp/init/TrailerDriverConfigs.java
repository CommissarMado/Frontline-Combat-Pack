package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Trailers.TrailerDriverData;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * TrailerDriverConfigs — datapack registry of hitch points for towing vehicles.
 *
 * Reads:  data/<namespace>/trailer_driver/<entity_id>.json
 * Keyed by the vehicle's entity registry id (namespace + path).
 * Reloads automatically on /reload — tune hitch offsets without a restart.
 *
 *   TrailerDriverData d = TrailerDriverConfigs.get(new ResourceLocation("fcp", "kamaz"));
 *   boolean canTow       = TrailerDriverConfigs.has(new ResourceLocation("fcp", "kamaz"));
 *
 * Returns null when a vehicle has no hitch config — null means "cannot tow".
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public class TrailerDriverConfigs {

    private static final String FOLDER = "trailer_driver";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, TrailerDriverData> CONFIGS = new HashMap<>();

    public static TrailerDriverData get(ResourceLocation entityId) {
        return CONFIGS.get(entityId);
    }

    public static boolean has(ResourceLocation entityId) {
        return CONFIGS.containsKey(entityId);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Map<ResourceLocation, TrailerDriverData>>() {

            @Override
            protected Map<ResourceLocation, TrailerDriverData> prepare(
                    ResourceManager manager, ProfilerFiller profiler) {

                Map<ResourceLocation, TrailerDriverData> loaded = new HashMap<>();

                for (Map.Entry<ResourceLocation, Resource> entry :
                        manager.listResources(FOLDER,
                                path -> path != null && path.getPath().endsWith(".json")
                        ).entrySet()) {

                    ResourceLocation location = entry.getKey();
                    Resource resource = entry.getValue();
                    if (location == null || resource == null) continue;

                    String rawPath = location.getPath();
                    if (rawPath == null) continue;

                    try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                        JsonElement json = JsonParser.parseReader(reader);

                        TrailerDriverData config = TrailerDriverData.CODEC
                                .parse(JsonOps.INSTANCE, json)
                                .getOrThrow(false, err ->
                                        LOGGER.error("[FCP] Bad trailer_driver config {}: {}", location, err));

                        String stripped = rawPath
                                .replace(FOLDER + "/", "")
                                .replace(".json", "");

                        ResourceLocation key = new ResourceLocation(location.getNamespace(), stripped);
                        loaded.put(key, config);
                        LOGGER.debug("[FCP] Loaded trailer_driver config: {}", key);

                    } catch (Exception e) {
                        LOGGER.error("[FCP] Failed to load trailer_driver config {}: {}", location, e.getMessage());
                    }
                }
                return loaded;
            }

            @Override
            protected void apply(Map<ResourceLocation, TrailerDriverData> loaded,
                                 ResourceManager manager, ProfilerFiller profiler) {
                CONFIGS.clear();
                CONFIGS.putAll(loaded);
                LOGGER.info("[FCP] Loaded {} trailer_driver config(s)", CONFIGS.size());
            }
        });
    }
}