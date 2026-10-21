package frontline.combat.fcp;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Mod configuration (config/fcp-common.toml).
 *
 * multicrew_vehicles switches every FCP vehicle's SBW data to the multicrew variant by
 * force-enabling the built-in "fcp_multicrew" datapack (see FCPPackFinder). Datapack
 * layering does the rest: only vehicles with a JSON in that pack change, everything else
 * falls through to the single-crew defaults. Takes effect on world load or /reload.
 */
public class FCPConfig {

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue MULTICREW;
    private static final ForgeConfigSpec.IntValue STUGNA_LINK_RANGE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("vehicles");
        MULTICREW = builder
                .comment("Use multicrew vehicle configurations (separate driver/gunner seats)",
                        "instead of the single-crew defaults where one person runs everything.",
                        "Applies on world load or /reload.")
                .define("multicrew_vehicles", false);
        builder.pop();
        builder.push("stugna");
        STUGNA_LINK_RANGE = builder
                .comment("How far (in blocks) a Stugna-P control panel searches for a launcher to link to.",
                        "The nearest free launcher within this range is used.")
                .defineInRange("link_range", 16, 1, 64);
        builder.pop();
        SPEC = builder.build();
    }

    /** Stugna control panel link range in blocks; the default until the config has loaded. */
    public static int stugnaLinkRange() {
        return SPEC.isLoaded() ? STUGNA_LINK_RANGE.get() : 16;
    }

    /** Safe at any time: false until the config file has actually loaded. */
    public static boolean multicrewEnabled() {
        return SPEC.isLoaded() && MULTICREW.get();
    }

    /** Set and persist. Callers are responsible for the permission check and pack reload. */
    public static void setMulticrew(boolean value) {
        if (!SPEC.isLoaded()) return;
        MULTICREW.set(value);
        SPEC.save();
    }
}