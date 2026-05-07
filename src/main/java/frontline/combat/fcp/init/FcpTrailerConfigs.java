package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.vehicle.Trailers.TrailerConfig;
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
 * FcpTrailerConfigs — loads all trailer JSON configs from datapacks.
 *
 * Reads from: data/<namespace>/trailers/<id>.json
 * Reloads automatically on /reload.
 *
 * Usage: FcpTrailerConfigs.get(new ResourceLocation("fcp", "my_trailer"))
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public class FcpTrailerConfigs {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, TrailerConfig> CONFIGS = new HashMap<>();

    /**
     * Returns the TrailerConfig for the given id.
     * Throws if no config is found — check your JSON path and spelling.
     */
    public static TrailerConfig get(ResourceLocation id) {
        TrailerConfig cfg = CONFIGS.get(id);
        if (cfg == null) {
            throw new IllegalStateException(
                    "[FCP] No trailer config found for: " + id
                            + " — expected at: data/" + id.getNamespace() + "/trailers/" + id.getPath() + ".json"
            );
        }
        return cfg;
    }

    /** Returns true if a config is registered for the given id. */
    public static boolean has(ResourceLocation id) {
        return CONFIGS.containsKey(id);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Map<ResourceLocation, TrailerConfig>>() {

            @Override
            protected Map<ResourceLocation, TrailerConfig> prepare(ResourceManager manager, ProfilerFiller profiler) {
                Map<ResourceLocation, TrailerConfig> loaded = new HashMap<>();

                // listResources can produce null keys from malformed resource pack entries —
                // iterate over entrySet() so we can explicitly guard both key and value.
                for (Map.Entry<ResourceLocation, Resource> entry :
                        manager.listResources("trailers", path -> {
                            // Guard against null path before calling getPath()
                            if (path == null) return false;
                            return path.getPath().endsWith(".json");
                        }).entrySet()) {

                    ResourceLocation location = entry.getKey();
                    Resource resource = entry.getValue();

                    // Skip any null keys that a broken resource pack may have introduced
                    if (location == null) {
                        LOGGER.warn("[FCP] Skipping trailer resource with null location key");
                        continue;
                    }

                    if (resource == null) {
                        LOGGER.warn("[FCP] Skipping null resource at {}", location);
                        continue;
                    }

                    try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                        JsonElement json = JsonParser.parseReader(reader);

                        TrailerConfig config = TrailerConfig.CODEC
                                .parse(JsonOps.INSTANCE, json)
                                .getOrThrow(false, err ->
                                        LOGGER.error("[FCP] Bad trailer config {}: {}", location, err));

                        // Strip "trailers/" prefix and ".json" suffix for the registry key
                        String rawPath = location.getPath();
                        if (rawPath == null) {
                            LOGGER.warn("[FCP] Trailer location has null path: {}", location);
                            continue;
                        }

                        String strippedPath = rawPath
                                .replace("trailers/", "")
                                .replace(".json", "");

                        ResourceLocation key = new ResourceLocation(location.getNamespace(), strippedPath);
                        loaded.put(key, config);
                        LOGGER.debug("[FCP] Loaded trailer config: {}", key);

                    } catch (Exception e) {
                        LOGGER.error("[FCP] Failed to load trailer config {}: {}", location, e.getMessage());
                    }
                }

                return loaded;
            }

            @Override
            protected void apply(Map<ResourceLocation, TrailerConfig> loaded,
                                 ResourceManager manager, ProfilerFiller profiler) {
                CONFIGS.clear();
                CONFIGS.putAll(loaded);
                LOGGER.info("[FCP] Loaded {} trailer config(s)", CONFIGS.size());
            }
        });
    }
}