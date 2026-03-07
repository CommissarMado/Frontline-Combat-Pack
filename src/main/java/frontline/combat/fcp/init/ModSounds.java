package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> REGISTRY =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FCP.MODID);

    public static final RegistryObject<SoundEvent> SPRAY = register("spray");

    // BMP-1
    public static final RegistryObject<SoundEvent> BMP1_FIRE_1P = register("bmp1_fire_1p");
    public static final RegistryObject<SoundEvent> BMP1_FIRE_3P = register("bmp1_fire_3p");
    public static final RegistryObject<SoundEvent> BMP1_FIRE_3P_FAR = register("bmp1_fire_3p_far");
    public static final RegistryObject<SoundEvent> BMP1_FIRE_3P_EXTRAFAR = register("bmp1_fire_3p_extrafar");
    public static final RegistryObject<SoundEvent> BMP1_MALYUTKA_FIRE_1P = register("bmp1_malyutka_fire_1p");
    public static final RegistryObject<SoundEvent> BMP1_MALYUTKA_FIRE_3P = register("bmp1_malyutka_fire_3p");
    public static final RegistryObject<SoundEvent> BMP1_MALYUTKA_FIRE_3P_FAR = register("bmp1_malyutka_fire_3p_far");
    public static final RegistryObject<SoundEvent> BMP1_CANNON_RELOAD = register("bmp1_cannon_reload");
    public static final RegistryObject<SoundEvent> BMP1_MALYUTKA_RELOAD = register("bmp1_malyutka_reload");
    public static final RegistryObject<SoundEvent> BMP1_ENGINE = register("bmp1_engine");
    public static final RegistryObject<SoundEvent> BMP1_INTO_CANNON = register("bmp1_into_cannon");
    public static final RegistryObject<SoundEvent> BMP1_INTO_MALYUTKA = register("bmp1_into_malyutka");

    // === Coax's ===
    public static final RegistryObject<SoundEvent> COAX_EQUIP = register("coax_equip");

    // Russian Coax
    public static final RegistryObject<SoundEvent> RUSSIAN_COAX__1P = register("russian_coax_1p");
    public static final RegistryObject<SoundEvent> RUSSIAN_COAX_3P = register("russian_coax_3p");
    public static final RegistryObject<SoundEvent> RUSSIAN_COAX_3P_FAR = register("russian_coax_3p_far");

    // Toyota's
    public static final RegistryObject<SoundEvent> TOYOTA_ENGINE = register("toyota_engine");

    // Spg-9 Technical
    public static final RegistryObject<SoundEvent> SPG9_FIRE_1P = register("spg9_fire_1p");
    public static final RegistryObject<SoundEvent> SPG9_FIRE_3P = register("spg9_fire_3p");
    public static final RegistryObject<SoundEvent> SPG9_RELOAD = register("spg9_reload");

    // Littlebird's
    public static final RegistryObject<SoundEvent> LITTLEBIRD_ENGINE_IDLE = register("littlebird_engine_idle");
    public static final RegistryObject<SoundEvent> LITTLEBIRD_ENGINE_START = register("littlebird_engine_start");

    // M134
    public static final RegistryObject<SoundEvent> M134_FIRE_1P = register("m134_fire_1p");
    public static final RegistryObject<SoundEvent> M134_FIRE_3P = register("m134_fire_3p");
    public static final RegistryObject<SoundEvent> M134_FIRE_3P_FAR = register("m134_fire_3p_far");

    // Stryker's
    public static final RegistryObject<SoundEvent> STRYKER_ENGINE = register("stryker_engine");

    // Stryker MGS
    public static final RegistryObject<SoundEvent> STRYKER_MGS_FIRE_1P = register("stryker_mgs_fire_1p");
    public static final RegistryObject<SoundEvent> STRYKER_MGS_FIRE_3P = register("stryker_mgs_fire_3p");
    public static final RegistryObject<SoundEvent> STRYKER_MGS_FIRE_3P_FAR = register("stryker_mgs_fire_3p_far");
    public static final RegistryObject<SoundEvent> STRYKER_MGS_FIRE_3P_EXTRAFAR = register("stryker_mgs_fire_3p_extrafar");
    public static final RegistryObject<SoundEvent> STRYKER_MGS_RELOAD = register("stryker_mgs_reload");

    // Stryker M2
    public static final RegistryObject<SoundEvent> M2_FIRE_1P = register("m2_fire_1p");
    public static final RegistryObject<SoundEvent> M2_FIRE_3P = register("m2_fire_3p");
    public static final RegistryObject<SoundEvent> M2_FIRE_3P_FAR = register("m2_fire_3p_far");
    public static final RegistryObject<SoundEvent> M2_RELOAD = register("m2_reload");

    // Lav25
    public static final RegistryObject<SoundEvent> LAV25_FIRE_1P = register("lav25_fire_1p");
    public static final RegistryObject<SoundEvent> LAV25_FIRE_3P = register("lav25_fire_3p");
    public static final RegistryObject<SoundEvent> LAV25_FIRE_3P_FAR = register("lav25_fire_3p_far");
    public static final RegistryObject<SoundEvent> LAV25_RELOAD = register("lav25_engine");

    // Tigr
    public static final RegistryObject<SoundEvent> TIGR_ENGINE = register("tigr_engine");

    // MK-19
    public static final RegistryObject<SoundEvent> MK19_FIRE_1P = register("mk19_fire_1p");
    public static final RegistryObject<SoundEvent> MK19_FIRE_3P = register("mk19_fire_3p");
    public static final RegistryObject<SoundEvent> MK19_FIRE_3P_FAR = register("mk19_fire_3p_far");
    public static final RegistryObject<SoundEvent> MK19_FIRE_3P_VERY_FAR = register("mk19_fire_3p_very_far");
    public static final RegistryObject<SoundEvent> MK19_RELOAD = register("mk19_engine");

    // Funny
    public static final RegistryObject<SoundEvent> WE_GOT_HIM = register("we_got_him");
    public static final RegistryObject<SoundEvent> BOMB_IRAN = register("bomb_iran");
    public static final RegistryObject<SoundEvent> GOD_SYRIA_AND_BASHAR = register("god_syria_and_bashar");
    public static final RegistryObject<SoundEvent> FUNKY_TOWN = register("funky_town");
    public static final RegistryObject<SoundEvent> ERIKA_TRAP_REMIX = register("erika_trap_remix");
    public static final RegistryObject<SoundEvent> ERIKA = register("erika");
    private static RegistryObject<SoundEvent> register(String name) {
        return REGISTRY.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("fcp", name)));
    }
    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
