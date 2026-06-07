package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, FCP.MODID);

    public static final Supplier<SoundEvent> SPRAY = register("spray");
    public static final Supplier<SoundEvent> BMP1_FIRE_1P = register("bmp1_fire_1p");
    public static final Supplier<SoundEvent> BMP1_FIRE_3P = register("bmp1_fire_3p");
    public static final Supplier<SoundEvent> BMP1_FIRE_3P_FAR = register("bmp1_fire_3p_far");
    public static final Supplier<SoundEvent> BMP1_FIRE_3P_EXTRAFAR = register("bmp1_fire_3p_extrafar");
    public static final Supplier<SoundEvent> BMP1_MALYUTKA_FIRE_1P = register("bmp1_malyutka_fire_1p");
    public static final Supplier<SoundEvent> BMP1_MALYUTKA_FIRE_3P = register("bmp1_malyutka_fire_3p");
    public static final Supplier<SoundEvent> BMP1_MALYUTKA_FIRE_3P_FAR = register("bmp1_malyutka_fire_3p_far");
    public static final Supplier<SoundEvent> BMP1_CANNON_RELOAD = register("bmp1_cannon_reload");
    public static final Supplier<SoundEvent> BMP1_MALYUTKA_RELOAD = register("bmp1_malyutka_reload");
    public static final Supplier<SoundEvent> BMP1_ENGINE = register("bmp1_engine");
    public static final Supplier<SoundEvent> BMP1_INTO_CANNON = register("bmp1_into_cannon");
    public static final Supplier<SoundEvent> BMP1_INTO_MALYUTKA = register("bmp1_into_malyutka");
    public static final Supplier<SoundEvent> COAX_EQUIP = register("coax_equip");
    public static final Supplier<SoundEvent> RUSSIAN_COAX__1P = register("russian_coax_1p");
    public static final Supplier<SoundEvent> RUSSIAN_COAX_3P = register("russian_coax_3p");
    public static final Supplier<SoundEvent> RUSSIAN_COAX_3P_FAR = register("russian_coax_3p_far");
    public static final Supplier<SoundEvent> TOYOTA_ENGINE = register("toyota_engine");
    public static final Supplier<SoundEvent> SPG9_FIRE_1P = register("spg9_fire_1p");
    public static final Supplier<SoundEvent> SPG9_FIRE_3P = register("spg9_fire_3p");
    public static final Supplier<SoundEvent> SPG9_RELOAD = register("spg9_reload");
    public static final Supplier<SoundEvent> LITTLEBIRD_ENGINE_IDLE = register("littlebird_engine_idle");
    public static final Supplier<SoundEvent> LITTLEBIRD_ENGINE_START = register("littlebird_engine_start");
    public static final Supplier<SoundEvent> M134_FIRE_1P = register("m134_fire_1p");
    public static final Supplier<SoundEvent> M134_FIRE_3P = register("m134_fire_3p");
    public static final Supplier<SoundEvent> M134_FIRE_3P_FAR = register("m134_fire_3p_far");
    public static final Supplier<SoundEvent> STRYKER_ENGINE = register("stryker_engine");
    public static final Supplier<SoundEvent> STRYKER_MGS_FIRE_1P = register("stryker_mgs_fire_1p");
    public static final Supplier<SoundEvent> STRYKER_MGS_FIRE_3P = register("stryker_mgs_fire_3p");
    public static final Supplier<SoundEvent> STRYKER_MGS_FIRE_3P_FAR = register("stryker_mgs_fire_3p_far");
    public static final Supplier<SoundEvent> STRYKER_MGS_FIRE_3P_EXTRAFAR = register("stryker_mgs_fire_3p_extrafar");
    public static final Supplier<SoundEvent> STRYKER_MGS_RELOAD = register("stryker_mgs_reload");
    public static final Supplier<SoundEvent> M2_FIRE_1P = register("m2_fire_1p");
    public static final Supplier<SoundEvent> M2_FIRE_3P = register("m2_fire_3p");
    public static final Supplier<SoundEvent> M2_FIRE_3P_FAR = register("m2_fire_3p_far");
    public static final Supplier<SoundEvent> M2_RELOAD = register("m2_reload");
    public static final Supplier<SoundEvent> LAV25_FIRE_1P = register("lav25_fire_1p");
    public static final Supplier<SoundEvent> LAV25_FIRE_3P = register("lav25_fire_3p");
    public static final Supplier<SoundEvent> LAV25_FIRE_3P_FAR = register("lav25_fire_3p_far");
    public static final Supplier<SoundEvent> LAV25_RELOAD = register("lav25_engine");
    public static final Supplier<SoundEvent> TIGR_ENGINE = register("tigr_engine");
    public static final Supplier<SoundEvent> MK19_FIRE_1P = register("mk19_fire_1p");
    public static final Supplier<SoundEvent> MK19_FIRE_3P = register("mk19_fire_3p");
    public static final Supplier<SoundEvent> MK19_FIRE_3P_FAR = register("mk19_fire_3p_far");
    public static final Supplier<SoundEvent> MK19_FIRE_3P_VERY_FAR = register("mk19_fire_3p_very_far");
    public static final Supplier<SoundEvent> MK19_RELOAD = register("mk19_engine");
    public static final Supplier<SoundEvent> WE_GOT_HIM = register("we_got_him");
    public static final Supplier<SoundEvent> BOMB_IRAN = register("bomb_iran");
    public static final Supplier<SoundEvent> GOD_SYRIA_AND_BASHAR = register("god_syria_and_bashar");
    public static final Supplier<SoundEvent> FUNKY_TOWN = register("funky_town");
    public static final Supplier<SoundEvent> ERIKA_TRAP_REMIX = register("erika_trap_remix");
    public static final Supplier<SoundEvent> ERIKA = register("erika");

    private static Supplier<SoundEvent> register(String name) {
        return REGISTRY.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FCP.MODID, name)));
    }

    public static void register(IEventBus eventBus) { REGISTRY.register(eventBus); }
}
