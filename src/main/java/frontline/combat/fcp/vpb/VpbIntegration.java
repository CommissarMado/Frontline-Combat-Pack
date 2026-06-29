package frontline.combat.fcp.compat.vpb;

import com.mojang.logging.LogUtils;
import frontline.combat.fcp.FCP;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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

    @Mod.EventBusSubscriber(modid = FCP.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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

    @Mod.EventBusSubscriber(modid = FCP.MODID)
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
