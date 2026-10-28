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
                    String type = s.has("type") && !s.get("type").getAsString().isBlank()
                            ? s.get("type").getAsString().trim().toLowerCase(java.util.Locale.ROOT) : null;
                    float pen = s.has("penetration") ? s.get("penetration").getAsFloat() : 0f;
                    float blockDamage = s.has("blockDamage") ? s.get("blockDamage").getAsFloat() : -1f;
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
                    WarheadStats stats = new WarheadStats(directDamage, explosionDamage, explosionRadius, particle, fireTime, destroy, type, pen, blockDamage);
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

    private static void warhead(JsonObject warheads, String id, float direct, float blast, float radius,
                                boolean destroyBlocks, String type, float penetration) {
        JsonObject entry = new JsonObject();
        entry.addProperty("directDamage", direct);
        entry.addProperty("explosionDamage", blast);
        entry.addProperty("explosionRadius", radius);
        entry.addProperty("explosionParticle", "MEDIUM");
        entry.addProperty("fireTime", 0);
        entry.addProperty("destroyBlocks", destroyBlocks);
        entry.addProperty("type", type);
        entry.addProperty("penetration", penetration);
        warheads.add(id, entry);
    }

    private static void writeDefault(Path path) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("comment", "FCP  Point Blank integration. Keys are registry ids. projectileWarheads maps a VPB projectile ENTITY id to the SBW warhead it is replaced by on impact: directDamage is a flat hit on the struck entity (runs through SBW vehicle DamageModifiers/armor), explosionDamage/explosionRadius are the AoE blast. AP = direct only; HE = explosion only; HEAT = both. type (heat, hat, explosive, frag, kinetic) and penetration (mm) feed FCP's vehicle armour model; blockDamage (0-1) shrinks the crater (frag defaults to 0.35). muzzleSmokeProjectiles lists VPB projectile ENTITY ids that emit the SBW-RPG muzzle + downrange smoke when fired. Enable debugLogging to print unmapped pointblank ids to the log so you can discover the exact strings for your installed content packs.");
        root.addProperty("debugLogging", false);
        JsonObject warheads = new JsonObject();
        // Squad launcher values x0.097 (rifle 62 -> 6). SBW blasts reach twice their radius, so radius = Squad outer radius / 2.
        // Columns: direct, blast, radius, destroyBlocks, armour type, penetration (mm).
        // Penetration = Squad's PROJECTILE value (the launcher-level ArmorPenetrationDepthMillimeters is stale, e.g. RPG-28
        // launcher 320 vs projectile 1200). Panzerfaust 3 isn't in Squad.
        warhead(warheads, "pointblank:example_rocket", 93.1f, 12.6f, 4f, true, "heat", 320);
        warhead(warheads, "pointblank:fcl_rpg7v2_pg7vm", 93.1f, 12.6f, 4f, false, "heat", 400);
        warhead(warheads, "pointblank:fcl_rpg7v2_pg7vr", 135.8f, 19.4f, 5f, true, "hat", 900);
        warhead(warheads, "pointblank:fcl_rpg7v2_og7v", 1.0f, 24.3f, 8f, false, "frag", 9);
        warhead(warheads, "pointblank:fcl_smaw_heaa", 135.8f, 19.4f, 5f, true, "hat", 900);
        warhead(warheads, "pointblank:fcl_smaw_hedm", 93.1f, 19.4f, 6f, true, "heat", 150);
        warhead(warheads, "pointblank:fcl_rpg26_rocket", 93.1f, 12.6f, 4f, false, "heat", 440);
        warhead(warheads, "pointblank:fcl_m72_rocket", 93.1f, 12.6f, 4f, false, "heat", 320);
        warhead(warheads, "pointblank:fcl_rpg28_rocket", 135.8f, 19.4f, 5f, true, "hat", 1200);
        warhead(warheads, "pointblank:fcl_at4_rocket", 93.1f, 12.6f, 4f, false, "heat", 420);
        warhead(warheads, "pointblank:fcl_dzj08_round", 93.1f, 12.6f, 4f, false, "heat", 400);
        warhead(warheads, "pointblank:grenade40mm", 5.8f, 14.8f, 5f, true, "frag", 5);
        warhead(warheads, "pointblank:cs_grenade30mm", 5.8f, 14.8f, 5f, true, "frag", 5);
        warhead(warheads, "pointblank:cs_grenade7g23", 5.8f, 14.8f, 5f, true, "frag", 5);
        warhead(warheads, "pointblank:fcl_carlgustaf_he448", 93.1f, 24.3f, 8f, true, "frag", 9);
        warhead(warheads, "pointblank:fcl_carlgustaf_heat551crs", 93.1f, 12.6f, 4f, false, "heat", 400);
        warhead(warheads, "pointblank:fcl_carlgustaf_heat758", 135.8f, 19.4f, 5f, true, "hat", 900);
        warhead(warheads, "pointblank:fcl_carlgustaf_hedp502", 93.1f, 19.4f, 6f, true, "heat", 150);
        warhead(warheads, "pointblank:fcl_rpg29_og29", 1.0f, 24.3f, 8f, true, "frag", 9);
        warhead(warheads, "pointblank:fcl_rpg29_pg29v", 135.8f, 19.4f, 5f, true, "hat", 900);
        warhead(warheads, "pointblank:fcl_rpg29_mg29", 93.1f, 19.4f, 6f, true, "heat", 150);
        warhead(warheads, "pointblank:fcl_panzerfaust3_bunkerfaust", 23.3f, 19.4f, 12f, true, "explosive", 12);
        warhead(warheads, "pointblank:fcl_panzerfaust3_tandem", 135.8f, 19.4f, 5f, true, "hat", 900);
        warhead(warheads, "pointblank:fcl_panzerfaust3_heat", 93.1f, 12.6f, 4f, false, "heat", 700);
        warhead(warheads, "pointblank:fcl_pf98_he", 1.0f, 24.3f, 8f, true, "frag", 9);
        warhead(warheads, "pointblank:fcl_pf98_heat", 135.8f, 19.4f, 5f, true, "hat", 900);
        warhead(warheads, "pointblank:fcl_pf98_hei", 93.1f, 19.4f, 6f, true, "heat", 150);
        root.add("projectileWarheads", warheads);
        JsonArray smokeProjectiles = new JsonArray();
        smokeProjectiles.add("pointblank:example_rocket");
        smokeProjectiles.add("pointblank:fcl_at4_rocket");
        smokeProjectiles.add("pointblank:fcl_rpg26_rocket");
        smokeProjectiles.add("pointblank:fcl_smaw_hedm");
        smokeProjectiles.add("pointblank:fcl_smaw_heaa");
        smokeProjectiles.add("pointblank:fcl_rpg7v2_pg7vm");
        smokeProjectiles.add("pointblank:fcl_rpg7v2_pg7vr");
        smokeProjectiles.add("pointblank:fcl_rpg7v2_og7v");
        smokeProjectiles.add("pointblank:fcl_rpg28_rocket");
        smokeProjectiles.add("pointblank:fcl_m72_rocket");
        smokeProjectiles.add("pointblank:fcl_dzj08_round");
        smokeProjectiles.add("pointblank:fcl_carlgustaf_he448");
        smokeProjectiles.add("pointblank:fcl_carlgustaf_heat551crs");
        smokeProjectiles.add("pointblank:fcl_carlgustaf_heat758");
        smokeProjectiles.add("pointblank:fcl_carlgustaf_hedp502");
        smokeProjectiles.add("pointblank:fcl_rpg29_og29");
        smokeProjectiles.add("pointblank:fcl_rpg29_pg29v");
        smokeProjectiles.add("pointblank:fcl_rpg29_mg29");
        smokeProjectiles.add("pointblank:fcl_panzerfaust3_bunkerfaust");
        smokeProjectiles.add("pointblank:fcl_panzerfaust3_tandem");
        smokeProjectiles.add("pointblank:fcl_panzerfaust3_heat");
        smokeProjectiles.add("pointblank:fcl_pf98_he");
        smokeProjectiles.add("pointblank:fcl_pf98_heat");
        smokeProjectiles.add("pointblank:fcl_pf98_hei");
        smokeProjectiles.add("pointblank:cs_m202_rocket");
        root.add("muzzleSmokeProjectiles", smokeProjectiles);

        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
    }
}
