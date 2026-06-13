package frontline.combat.fcp;

import com.mojang.logging.LogUtils;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.init.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
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

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onItemTooltip);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FCPNetwork.register();
        });
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
