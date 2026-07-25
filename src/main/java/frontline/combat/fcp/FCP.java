package frontline.combat.fcp;

import com.mojang.logging.LogUtils;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.init.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(FCP.MODID)
public class FCP {
    public static final String MODID = "fcp";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final net.minecraftforge.network.simple.SimpleChannel PACKET_HANDLER = FCPNetwork.FCP_HANDLER;

    public FCP() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.register(modEventBus);
        ModItems.REGISTRY.register(modEventBus);
        ModParticleTypes.PARTICLE_TYPES.register(modEventBus);
        ModSounds.REGISTRY.register(modEventBus);
        ModTabs.TABS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::setup);
        // Client-only setup (never fires on a dedicated server)
        modEventBus.addListener(this::clientSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onItemTooltip);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FCPNetwork.register();
        });
    }

    /**
     * Client-side setup.
     *
     * Registers the handler that redirects "open your inventory" to the vehicle's hold while
     * you're riding one. It's also self-registering via @Mod.EventBusSubscriber, and the
     * register() call is guarded, so doing it here as well is harmless — this is just the
     * path that doesn't depend on annotation scanning.
     *
     * DistExecutor keeps the client-only class off the dedicated server entirely, so the
     * server never even loads it.
     */
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> frontline.combat.fcp.client.VehicleInventoryKeyHandler::register));
    }

    private void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof BlockItem && event.getItemStack().hasTag()) {
            CompoundTag tag = BlockItem.getBlockEntityData(event.getItemStack());
            if (tag != null && tag.contains("EntityType")) {
                String entityType = tag.getString("EntityType");
                if (entityType.startsWith(MODID + ":vdv_")) {
                    // event.getToolTip().add(Component.translatable("tooltip.wmp.model_author"));
                    event.getToolTip().add(Component.translatable("tooltip.fcp.usage_restriction").withStyle(net.minecraft.ChatFormatting.RED));
                }
            }
        }
    }

    // Helper method to create ResourceLocation for this mod
    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MODID, path);
    }
}