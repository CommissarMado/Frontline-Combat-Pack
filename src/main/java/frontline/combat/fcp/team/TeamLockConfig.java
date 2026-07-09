package frontline.combat.fcp.team;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class TeamLockConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "fcp-team-lock.json";

    private static volatile TeamLockConfig INSTANCE = new TeamLockConfig();

    public final boolean enforce;
    public final boolean autoClaimOnEnter;
    public final boolean blockUnclaimed;
    public final boolean opBypass;
    public final boolean applyToNonFcpVehicles;
    public final int messageCooldownTicks;

    public final Set<String> interactionBypassItems;

    private TeamLockConfig() {
        this(true, true, false, true, true, 40, Collections.emptySet());
    }

    private TeamLockConfig(boolean enforce, boolean autoClaimOnEnter, boolean blockUnclaimed,
                           boolean opBypass, boolean applyToNonFcpVehicles, int messageCooldownTicks,
                           Set<String> interactionBypassItems) {
        this.enforce = enforce;
        this.autoClaimOnEnter = autoClaimOnEnter;
        this.blockUnclaimed = blockUnclaimed;
        this.opBypass = opBypass;
        this.applyToNonFcpVehicles = applyToNonFcpVehicles;
        this.messageCooldownTicks = messageCooldownTicks;
        this.interactionBypassItems = Collections.unmodifiableSet(interactionBypassItems);
    }

    public static TeamLockConfig get() {
        return INSTANCE;
    }

    public static synchronized void load() {
        Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        try {
            if (!Files.exists(path)) {
                writeDefault(path);
                LOGGER.info("[FCP/Team] Created default team-lock config at {}", path);
            }

            String json = Files.readString(path, StandardCharsets.UTF_8);
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            if (root == null) {
                throw new IOException("config root is not a JSON object");
            }

            TeamLockConfig def = new TeamLockConfig();
            INSTANCE = new TeamLockConfig(
                    bool(root, "enforce", def.enforce),
                    bool(root, "autoClaimOnEnter", def.autoClaimOnEnter),
                    bool(root, "blockUnclaimed", def.blockUnclaimed),
                    bool(root, "opBypass", def.opBypass),
                    bool(root, "applyToNonFcpVehicles", def.applyToNonFcpVehicles),
                    root.has("messageCooldownTicks") ? root.get("messageCooldownTicks").getAsInt() : def.messageCooldownTicks,
                    stringSet(root, "interactionBypassItems")
            );

            LOGGER.info("[FCP/Team] Loaded team-lock config (enforce={}, autoClaim={}, blockUnclaimed={}, opBypass={}, allVehicles={}, bypassItems={}).",
                    INSTANCE.enforce, INSTANCE.autoClaimOnEnter, INSTANCE.blockUnclaimed, INSTANCE.opBypass,
                    INSTANCE.applyToNonFcpVehicles, INSTANCE.interactionBypassItems.size());
        } catch (Exception e) {
            LOGGER.error("[FCP/Team] Failed to load {}, falling back to defaults.", FILE_NAME, e);
            INSTANCE = new TeamLockConfig();
        }
    }

    private static boolean bool(JsonObject o, String key, boolean def) {
        return o.has(key) ? o.get(key).getAsBoolean() : def;
    }

    private static Set<String> stringSet(JsonObject o, String key) {
        Set<String> out = new HashSet<>();
        if (o.has(key) && o.get(key).isJsonArray()) {
            for (JsonElement el : o.getAsJsonArray(key)) {
                if (el.isJsonPrimitive()) out.add(el.getAsString());
            }
        }
        return out;
    }

    private static void writeDefault(Path path) throws IOException {
        TeamLockConfig d = new TeamLockConfig();
        JsonObject root = new JsonObject();
        root.addProperty("_comment", "Frontline Combat Pack - persistent per-vehicle team lock. Teams are vanilla scoreboard teams. Blocks ALL right-click interaction (enter, inventory, repaint, crowbar, name tag) with enemy vehicles; left-click attacks and gunfire are never affected.");
        root.addProperty("enforce", d.enforce);
        root.addProperty("autoClaimOnEnter", d.autoClaimOnEnter);
        root.addProperty("blockUnclaimed", d.blockUnclaimed);
        root.addProperty("opBypass", d.opBypass);
        root.addProperty("applyToNonFcpVehicles", d.applyToNonFcpVehicles);
        root.addProperty("messageCooldownTicks", d.messageCooldownTicks);
        root.addProperty("_interactionBypassItems_comment", "Item IDs allowed to interact with enemy vehicles anyway (e.g. keep C4 working): [\"superbwarfare:c4_bomb\", \"superbwarfare:detonator\"]. Empty = block everything.");
        root.add("interactionBypassItems", new JsonArray());
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
    }
}