package frontline.combat.fcp.compat.vpb;

import com.mojang.logging.LogUtils;
import frontline.combat.fcp.FCP;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

public final class VpbIntegration {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String VPB_MODID = "pointblank";

    private static boolean loaded;

    private VpbIntegration() {
    }

    public static boolean isVpbLoaded() {
        return loaded;
    }

    /** Small-arms penetration (mm) of Point Blank guns, matching FCP's vehicle MGs: rifle calibres / 12.7 mm and up. */
    private static final float GUN_PEN = 7f, HEAVY_GUN_PEN = 16f;
    /** Point Blank guns firing 12.7 mm or heavier (machine guns and anti-materiel rifles), by item id path. */
    private static final Set<String> HEAVY_GUNS = Set.of("dshkq", "m2q", "gm6lynx", "hissniper", "cs_6p62", "whizzbangq");
    private static final Map<Class<?>, Boolean> GUN_CLASSES = new ConcurrentHashMap<>();

    /**
     * Penetration (mm) for a hit from a Point Blank gun, or -1 if the hit isn't one. Point Blank bullets (hitscan and
     * its slow bullets alike) hurt with the plain player-attack damage type, so they are recognised by the shooter's
     * main-hand item being a Point Blank GunItem.
     */
    public static float gunHitPenetration(DamageSource source) {
        if (!loaded || !source.is(DamageTypes.PLAYER_ATTACK) || !(source.getEntity() instanceof LivingEntity shooter)) return -1f;
        ItemStack held = shooter.getMainHandItem();
        if (held.isEmpty() || !isGunItem(held.getItem().getClass())) return -1f;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(held.getItem());
        return id != null && HEAVY_GUNS.contains(id.getPath()) ? HEAVY_GUN_PEN : GUN_PEN;
    }

    private static boolean isGunItem(Class<?> cls) {
        return GUN_CLASSES.computeIfAbsent(cls, c -> {
            for (Class<?> k = c; k != null; k = k.getSuperclass()) if (k.getName().equals("com.vicmatskiv.pointblank.item.GunItem")) return true;
            return false;
        });
    }

    @EventBusSubscriber(modid = FCP.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            loaded = ModList.get().isLoaded(VPB_MODID);
            VpbIntegrationConfig.load();
            if (loaded) {
                LOGGER.info("[FCP/VPB] Point Blank detected - explosion replacement and muzzle smoke active.");
            } else {
                LOGGER.info("[FCP/VPB] Point Blank not installed - integration idle (FCP runs normally).");
            }
        }
    }

    @EventBusSubscriber(modid = FCP.MODID)
    public static final class ForgeBus {
        private ForgeBus() {
        }

        @SubscribeEvent
        public static void onServerAboutToStart(ServerAboutToStartEvent event) {
            // Pick up any edits made to the config file between launches.
            VpbIntegrationConfig.load();
        }

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            event.getDispatcher().register(
                    Commands.literal("fcpvpb")
                            .requires(src -> src.hasPermission(2))
                            .then(Commands.literal("reload").executes(ctx -> {
                                VpbIntegrationConfig.load();
                                VpbIntegrationConfig cfg = VpbIntegrationConfig.get();
                                ctx.getSource().sendSuccess(() -> Component.literal(
                                        "[FCP/VPB] Reloaded: " + cfg.projectileWarheads.size()
                                                + " warhead mapping(s), " + cfg.muzzleSmokeProjectiles.size()
                                                + " muzzle-smoke projectile(s)."), true);
                                return 1;
                            })));
        }
    }
}
