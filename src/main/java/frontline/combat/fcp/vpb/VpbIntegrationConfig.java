package frontline.combat.fcp.compat.vpb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.atsuishio.superbwarfare.tools.ParticleTool;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public final class VpbIntegrationConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "fcp-vpb-integration.json";

    private static volatile VpbIntegrationConfig INSTANCE = new VpbIntegrationConfig();

    public final Map<ResourceLocation, WarheadStats> projectileWarheads;
    public final Set<ResourceLocation> muzzleSmokeProjectiles;
    public final boolean debugLogging;

    private VpbIntegrationConfig() {
        this(new HashMap<>(), new HashSet<>(), false);
    }

    private VpbIntegrationConfig(Map<ResourceLocation, WarheadStats> projectileWarheads,
                                 Set<ResourceLocation> muzzleSmokeProjectiles,
                                 boolean debugLogging) {
        this.projectileWarheads = Collections.unmodifiableMap(projectileWarheads);
        this.muzzleSmokeProjectiles = Collections.unmodifiableSet(muzzleSmokeProjectiles);
        this.debugLogging = debugLogging;
    }


    public static VpbIntegrationConfig get() {
        return INSTANCE;
    }


    public static synchronized void load() {
        Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        try {
            if (!Files.exists(path)) {
                writeDefault(path);
                LOGGER.info("[FCP/VPB] Created default integration config at {}", path);
            }

            String json = Files.readString(path, StandardCharsets.UTF_8);
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            if (root == null) {
                throw new IOException("config root is not a JSON object");
            }

            boolean debug = root.has("debugLogging") && root.get("debugLogging").getAsBoolean();

            Map<ResourceLocation, WarheadStats> warheads = new HashMap<>();
            if (root.has("projectileWarheads") && root.get("projectileWarheads").isJsonObject()) {
                JsonObject obj = root.getAsJsonObject("projectileWarheads");
                for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
                    if (e.getKey().startsWith("_")) continue; // allow "_comment" style keys
                    ResourceLocation id = parseId(e.getKey());
                    if (id == null || !e.getValue().isJsonObject()) continue;
                    JsonObject s = e.getValue().getAsJsonObject();
                    float directDamage = s.has("directDamage") ? s.get("directDamage").getAsFloat() : 0f;
                    float explosionDamage = s.has("explosionDamage") ? s.get("explosionDamage").getAsFloat() : 0f;
                    float explosionRadius = s.has("explosionRadius") ? s.get("explosionRadius").getAsFloat() : 0f;
                    int fireTime = s.has("fireTime") ? s.get("fireTime").getAsInt() : 0;
                    boolean destroy = !s.has("destroyBlocks") || s.get("destroyBlocks").getAsBoolean();

                    ParticleTool.ParticleType particle = null;
                    if (s.has("explosionParticle") && !s.get("explosionParticle").getAsString().isBlank()) {
                        String raw = s.get("explosionParticle").getAsString().trim().toUpperCase(java.util.Locale.ROOT);
                        try {
                            particle = ParticleTool.ParticleType.valueOf(raw);
                        } catch (IllegalArgumentException badType) {
                            LOGGER.warn("[FCP/VPB] '{}' has invalid explosionParticle '{}' - using radius-based tier. Valid: MINI, SMALL, MEDIUM, LARGE, HUGE, GIANT",
                                    id, raw);
                        }
                    }

                    WarheadStats stats = new WarheadStats(directDamage, explosionDamage, explosionRadius, particle, fireTime, destroy);
                    if (!stats.hasDirectHit() && !stats.hasExplosion()) {
                        LOGGER.warn("[FCP/VPB] Skipping '{}': needs directDamage > 0 and/or (explosionDamage > 0 and explosionRadius > 0)", id);
                        continue;
                    }
                    warheads.put(id, stats);
                }
            }

            Set<ResourceLocation> smokeProjectiles = new HashSet<>();
            if (root.has("muzzleSmokeProjectiles") && root.get("muzzleSmokeProjectiles").isJsonArray()) {
                JsonArray arr = root.getAsJsonArray("muzzleSmokeProjectiles");
                for (JsonElement el : arr) {
                    ResourceLocation id = parseId(el.getAsString());
                    if (id != null) smokeProjectiles.add(id);
                }
            }

            INSTANCE = new VpbIntegrationConfig(warheads, smokeProjectiles, debug);
            LOGGER.info("[FCP/VPB] Loaded {} warhead mapping(s) and {} muzzle-smoke projectile(s).",
                    warheads.size(), smokeProjectiles.size());
        } catch (Exception ex) {
            LOGGER.warn("[FCP/VPB] Failed to read {} - integration disabled until fixed: {}",
                    FILE_NAME, ex.toString());
            INSTANCE = new VpbIntegrationConfig();
        }
    }

    private static ResourceLocation parseId(String raw) {
        if (raw == null) return null;
        ResourceLocation id = ResourceLocation.tryParse(raw.trim());
        if (id == null) {
            LOGGER.warn("[FCP/VPB] Ignoring invalid id '{}'", raw);
        }
        return id;
    }

    private static void writeDefault(Path path) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("comment",
                "FCP  Point Blank integration. Keys are registry ids. projectileWarheads maps a VPB "
                        + "projectile ENTITY id to the SBW warhead it is replaced by on impact: directDamage is "
                        + "a flat hit on the struck entity (runs through SBW vehicle DamageModifiers/armor), "
                        + "explosionDamage/explosionRadius are the AoE blast. AP = direct only; HE = explosion "
                        + "only; HEAT = both. muzzleSmokeProjectiles lists VPB projectile ENTITY ids that emit "
                        + "the SBW-RPG muzzle + downrange smoke when fired. Enable debugLogging to print unmapped "
                        + "pointblank ids "
                        + "to the log so you can discover the exact strings for your installed content packs.");
        root.addProperty("debugLogging", false);

        JsonObject warheads = new JsonObject();
        JsonObject example = new JsonObject();
        example.addProperty("directDamage", 200.0);
        example.addProperty("explosionDamage", 90.0);
        example.addProperty("explosionRadius", 6.0);
        example.addProperty("explosionParticle", "MEDIUM");
        example.addProperty("fireTime", 0);
        example.addProperty("destroyBlocks", true);
        warheads.add("pointblank:example_rocket", example);
        root.add("projectileWarheads", warheads);

        JsonArray smokeProjectiles = new JsonArray();
        smokeProjectiles.add("pointblank:example_rocket");
        root.add("muzzleSmokeProjectiles", smokeProjectiles);

        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
    }
}
