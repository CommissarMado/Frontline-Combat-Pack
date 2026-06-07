package frontline.combat.fcp;

import com.mojang.logging.LogUtils;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.init.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.slf4j.Logger;

@Mod(FCP.MODID)
// NeoForge 1.21.1 port by beihaimc
// Original: CommissarMado/Frontline-Combat-Pack
public class FCP {
    public static final String MODID = "fcp";
    private static final Logger LOGGER = LogUtils.getLogger();

    public FCP(IEventBus modEventBus) {
        ModEntities.register(modEventBus);
        ModItems.REGISTRY.register(modEventBus);
        ModSounds.REGISTRY.register(modEventBus);
        ModTabs.TABS.register(modEventBus);

        modEventBus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(FCPNetwork::register);
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof BlockItem && event.getItemStack().has(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA)) {
            var blockData = event.getItemStack().get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
            if (blockData != null) {
                CompoundTag tag = blockData.copyTag();
                if (tag != null && tag.contains("EntityType")) {
                    String entityType = tag.getString("EntityType");
                    if (entityType.startsWith(MODID + ":vdv_")) {
                        event.getToolTip().add(Component.translatable("tooltip.fcp.usage_restriction")
                                .withStyle(net.minecraft.ChatFormatting.RED));
                    }
                }
            }
        }
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
