package frontline.combat.fcp.vehicle.humvee;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.init.ModItems;
import frontline.combat.fcp.init.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4d;
import org.joml.Vector4d;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class HumveeAttachmentHandler {

    private static final double REACH = 5.0;

    private HumveeAttachmentHandler() {}

    @SubscribeEvent
    public static void onInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        cycle(event, event.getTarget(), event.getEntity(), event.getItemStack());
    }

    private static void cycle(PlayerInteractEvent event, Entity target, Player player, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != ModItems.SPRAY.get()) return;
        if (!(target instanceof HumveeVehicle humvee) || !(target instanceof VehicleEntity vehicle)) return;

        List<HumveeAttachments.Category> categories = HumveeAttachments.categories(humvee.humveeName());
        if (categories.isEmpty()) return;

        Vec3 eye = player.getEyePosition(1f);
        Vec3 end = eye.add(player.getViewVector(1f).scale(REACH));
        Matrix4d transform = VehicleVecUtils.INSTANCE.getVehicleYOffsetTransform(vehicle, 1f);
        double root = vehicle.getRotateOffsetHeight();

        HumveeAttachments.Category best = null;
        double bestDist = Double.MAX_VALUE;
        for (HumveeAttachments.Category c : categories) {
            AABB world = toWorldBox(c, transform, root);
            Optional<Vec3> hit = world.clip(eye, end);
            if (hit.isPresent()) {
                double d = hit.get().distanceToSqr(eye);
                if (d < bestDist) {
                    bestDist = d;
                    best = c;
                }
            }
        }

        // Nothing hit: leave the event for the vehicle's own spray handler (camo repaint).
        if (best == null) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (!player.level().isClientSide()) {
            humvee.cycleAttachment(best.name, best.variantCount);
            player.level().playSound(null, target.blockPosition(),
                    ModSounds.SPRAY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.swing(event.getHand());
    }

    private static AABB toWorldBox(HumveeAttachments.Category c, Matrix4d transform, double root) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (double[] corner : c.corners()) {
            Vector4d w = transform.transform(new Vector4d(corner[0], corner[1] - root, corner[2], 1.0));
            minX = Math.min(minX, w.x); maxX = Math.max(maxX, w.x);
            minY = Math.min(minY, w.y); maxY = Math.max(maxY, w.y);
            minZ = Math.min(minZ, w.z); maxZ = Math.max(maxZ, w.z);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
