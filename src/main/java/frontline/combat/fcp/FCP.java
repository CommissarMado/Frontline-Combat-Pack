package frontline.combat.fcp;

import com.mojang.logging.LogUtils;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.init.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(FCP.MODID)
public class FCP {
    public static final String MODID = "fcp";
    private static final Logger LOGGER = LogUtils.getLogger();

    public FCP(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, FCPConfig.SPEC);

        ModEntities.register(modEventBus);
        ModBlocks.REGISTRY.register(modEventBus);
        ModBlockEntities.REGISTRY.register(modEventBus);
        ModItems.REGISTRY.register(modEventBus);
        ModParticleTypes.PARTICLE_TYPES.register(modEventBus);
        ModSounds.REGISTRY.register(modEventBus);
        ModTabs.TABS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);

        // Network payloads
        modEventBus.addListener(FCPNetwork::register);
        // Client-only setup (never fires on a dedicated server)
        modEventBus.addListener(this::clientSetup);

        NeoForge.EVENT_BUS.addListener(this::onItemTooltip);
    }

    /**
     * Runs client-only code. The double lambda keeps the client classes it names off the
     * dedicated server: they are only loaded when the inner Runnable is created, which never
     * happens there.
     */
    public static void runOnClient(Supplier<Runnable> task) {
        if (FMLEnvironment.dist == Dist.CLIENT) task.get().run();
    }

    /** The block-entity NBT an item stack carries (SBW containers keep their vehicle there), or null. */
    public static @Nullable CompoundTag blockEntityData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        return data == null ? null : data.copyTag();
    }

    /**
     * Client-side setup.
     *
     * Registers the handler that redirects "open your inventory" to the vehicle's hold while
     * you're riding one. It's also self-registering via @EventBusSubscriber, and the
     * register() call is guarded, so doing it here as well is harmless - this is just the
     * path that doesn't depend on annotation scanning.
     */
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> runOnClient(() -> () -> {
            frontline.combat.fcp.client.VehicleInventoryKeyHandler.register();
            frontline.combat.fcp.client.screen.FCPConfigScreen.registerConfigScreen();
        }));
    }

    private void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof BlockItem) {
            CompoundTag tag = blockEntityData(event.getItemStack());
            if (tag != null && tag.contains("EntityType")) {
                String entityType = tag.getString("EntityType");
                if (entityType.startsWith(MODID + ":vdv_")) {
                    // event.getToolTip().add(Component.translatable("tooltip.wmp.model_author"));
                    event.getToolTip().add(Component.translatable("tooltip.fcp.usage_restriction").withStyle(net.minecraft.ChatFormatting.RED));
                }
            }
        }
    }

    /** Cancels an entity interaction and sets its result (only the concrete event types can be cancelled). */
    public static void cancelInteraction(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent event,
                                         net.minecraft.world.InteractionResult result) {
        if (event instanceof net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract e) {
            e.setCanceled(true);
            e.setCancellationResult(result);
        } else if (event instanceof net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific e) {
            e.setCanceled(true);
            e.setCancellationResult(result);
        }
    }

    // Helper method to create ResourceLocation for this mod
    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}