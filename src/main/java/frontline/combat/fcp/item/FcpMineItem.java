package frontline.combat.fcp.item;

import com.atsuishio.superbwarfare.item.projectile.Tm62Item;
import frontline.combat.fcp.entity.projectile.FcpMineEntity;
import frontline.combat.fcp.init.ModEntities;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * fcp:mine's item: SBW's TM-62 item (its held-item model comes along with Tm62Item) placing an {@link FcpMineEntity}
 * the way SBW's places a TM-62 - tossed half a block per tick along the look, random yaw.
 */
public class FcpMineItem extends Tm62Item {

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            FcpMineEntity mine = new FcpMineEntity(ModEntities.MINE.get(), level);
            mine.setOwnerUUID(player.getUUID());
            float yaw = (float) (Math.random() * 360 - 180);
            mine.moveTo(player.getX(), player.getY() + 1.1, player.getZ(), yaw, 0);
            mine.setYBodyRot(yaw);
            mine.setYHeadRot(yaw);
            Vec3 look = player.getLookAngle();
            mine.setDeltaMovement(0.5 * look.x, 0.5 * look.y, 0.5 * look.z);
            level.addFreshEntity(mine);
        }
        player.getCooldowns().addCooldown(this, 20);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResultHolder.success(stack);
    }
}
