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
 * FcpTrailerConfigs — loads trailer geometry configs.
 *
 * Reads from: data/<namespace>/trailers/<id>.json
 * Reloads automatically on /reload.
 *
 * Usage: FcpTrailerConfigs.get(new ResourceLocation("fcp", "example_trailer"))
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public class FcpTrailerConfigs {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, TrailerConfig> CONFIGS = new HashMap<>();

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

    public static boolean has(ResourceLocation id) {
        return CONFIGS.containsKey(id);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Map<ResourceLocation, TrailerConfig>>() {

            @Override
            protected Map<ResourceLocation, TrailerConfig> prepare(
                    ResourceManager manager, ProfilerFiller profiler) {

                Map<ResourceLocation, TrailerConfig> loaded = new HashMap<>();

                for (Map.Entry<ResourceLocation, Resource> entry :
                        manager.listResources("trailers",
                                path -> path != null && path.getPath().endsWith(".json")
                        ).entrySet()) {

                    ResourceLocation location = entry.getKey();
                    Resource resource = entry.getValue();

                    if (location == null || resource == null) continue;

                    String rawPath = location.getPath();
                    if (rawPath == null) continue;

                    try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                        JsonElement json = JsonParser.parseReader(reader);

                        TrailerConfig config = TrailerConfig.CODEC
                                .parse(JsonOps.INSTANCE, json)
                                .getOrThrow(false, err ->
                                        LOGGER.error("[FCP] Bad trailer config {}: {}", location, err));

                        String stripped = rawPath
                                .replace("trailers/", "")
                                .replace(".json", "");

                        ResourceLocation key = new ResourceLocation(location.getNamespace(), stripped);
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