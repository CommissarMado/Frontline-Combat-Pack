package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Trailers.TrailerTowedData;
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
 * TrailerTowedConfigs — datapack registry of tongue points / tow rules for trailers.
 *
 * Reads:  data/<namespace>/trailer_towed/<entity_id>.json
 * Keyed by the trailer's entity registry id (namespace + path).
 * Reloads automatically on /reload.
 *
 *   TrailerTowedData t = TrailerTowedConfigs.get(new ResourceLocation("fcp", "example_trailer"));
 *   boolean isTrailer   = TrailerTowedConfigs.has(new ResourceLocation("fcp", "example_trailer"));
 *
 * Returns null when a trailer entity has no towed config — null means it can't be hitched.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public class TrailerTowedConfigs {

    private static final String FOLDER = "trailer_towed";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, TrailerTowedData> CONFIGS = new HashMap<>();

    public static TrailerTowedData get(ResourceLocation entityId) {
        return CONFIGS.get(entityId);
    }

    public static boolean has(ResourceLocation entityId) {
        return CONFIGS.containsKey(entityId);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Map<ResourceLocation, TrailerTowedData>>() {

            @Override
            protected Map<ResourceLocation, TrailerTowedData> prepare(
                    ResourceManager manager, ProfilerFiller profiler) {

                Map<ResourceLocation, TrailerTowedData> loaded = new HashMap<>();

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

                        TrailerTowedData config = TrailerTowedData.CODEC
                                .parse(JsonOps.INSTANCE, json)
                                .getOrThrow(false, err ->
                                        LOGGER.error("[FCP] Bad trailer_towed config {}: {}", location, err));

                        String stripped = rawPath
                                .replace(FOLDER + "/", "")
                                .replace(".json", "");

                        ResourceLocation key = new ResourceLocation(location.getNamespace(), stripped);
                        loaded.put(key, config);
                        LOGGER.debug("[FCP] Loaded trailer_towed config: {}", key);

                    } catch (Exception e) {
                        LOGGER.error("[FCP] Failed to load trailer_towed config {}: {}", location, e.getMessage());
                    }
                }
                return loaded;
            }

            @Override
            protected void apply(Map<ResourceLocation, TrailerTowedData> loaded,
                                 ResourceManager manager, ProfilerFiller profiler) {
                CONFIGS.clear();
                CONFIGS.putAll(loaded);
                LOGGER.info("[FCP] Loaded {} trailer_towed config(s)", CONFIGS.size());
            }
        });
    }
}