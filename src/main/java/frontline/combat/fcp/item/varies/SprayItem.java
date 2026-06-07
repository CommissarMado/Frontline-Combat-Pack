package frontline.combat.fcp.item.varies;

import frontline.combat.fcp.entity.vehicle.ICamoVehicle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class SprayItem extends Item {
    public SprayItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void appendHoverText(ItemStack pStack, Item.TooltipContext context, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("des.fcp.spray").withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable("des.fcp.spray.usage").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, net.minecraft.world.entity.LivingEntity target, InteractionHand hand) {
        if (target instanceof ICamoVehicle camoVehicle) {
            if (!player.level().isClientSide) {
                camoVehicle.cycleCamo();

                String[] camoNames = camoVehicle.getCamoNames();
                int camoType = camoVehicle.getCamoType();
                String camoName = (camoType >= 0 && camoType < camoNames.length)
                        ? camoNames[camoType]
                        : "Unknown";

                player.displayClientMessage(
                        Component.translatable("message.fcp.camo_changed", camoName).withStyle(ChatFormatting.GREEN),
                        true
                );
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
