package frontline.combat.fcp.team;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

    private TeamLockConfig() {
        this(true, true, false, true, true, 40);
    }

    private TeamLockConfig(boolean enforce, boolean autoClaimOnEnter, boolean blockUnclaimed,
                           boolean opBypass, boolean applyToNonFcpVehicles, int messageCooldownTicks) {
        this.enforce = enforce;
        this.autoClaimOnEnter = autoClaimOnEnter;
        this.blockUnclaimed = blockUnclaimed;
        this.opBypass = opBypass;
        this.applyToNonFcpVehicles = applyToNonFcpVehicles;
        this.messageCooldownTicks = messageCooldownTicks;
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
                    root.has("messageCooldownTicks") ? root.get("messageCooldownTicks").getAsInt() : def.messageCooldownTicks
            );

            LOGGER.info("[FCP/Team] Loaded team-lock config (enforce={}, autoClaim={}, blockUnclaimed={}, opBypass={}, allVehicles={}).",
                    INSTANCE.enforce, INSTANCE.autoClaimOnEnter, INSTANCE.blockUnclaimed, INSTANCE.opBypass, INSTANCE.applyToNonFcpVehicles);
        } catch (Exception e) {
            LOGGER.error("[FCP/Team] Failed to load {}, falling back to defaults.", FILE_NAME, e);
            INSTANCE = new TeamLockConfig();
        }
    }

    private static boolean bool(JsonObject o, String key, boolean def) {
        return o.has(key) ? o.get(key).getAsBoolean() : def;
    }

    private static void writeDefault(Path path) throws IOException {
        TeamLockConfig d = new TeamLockConfig();
        JsonObject root = new JsonObject();
        root.addProperty("_comment", "Frontline Combat Pack - persistent per-vehicle team lock. Teams are vanilla scoreboard teams.");
        root.addProperty("enforce", d.enforce);
        root.addProperty("autoClaimOnEnter", d.autoClaimOnEnter);
        root.addProperty("blockUnclaimed", d.blockUnclaimed);
        root.addProperty("opBypass", d.opBypass);
        root.addProperty("applyToNonFcpVehicles", d.applyToNonFcpVehicles);
        root.addProperty("messageCooldownTicks", d.messageCooldownTicks);
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
    }
}
