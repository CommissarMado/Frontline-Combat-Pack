package frontline.combat.fcp.entity.vehicle.Ural;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.init.ModItems;
import frontline.combat.fcp.init.ModSounds;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4d;
import org.joml.Vector4d;

import java.util.Optional;

/**
 * Toggles the Ural's "TENTY" canopy the same way the humvee attachments work: right-click
 * the Ural with the FCP spray while looking at the canopy's hitbox. If the look ray misses
 * the box the spray is left alone so its default camo repaint still works.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class UralTentHandler {

    private static final double REACH = 5.0;
    // TENTY canopy hitbox, min/max in SuperbWarfare's vehicle-local space (derived from the geo).
    private static final double[] MIN = {-1.2112, 1.6203, -5.6877};
    private static final double[] MAX = {1.2113, 3.1535, 1.1694};

    private UralTentHandler() {}

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() != ModItems.SPRAY.get()) return;
        if (!(event.getTarget() instanceof UralEntity ural)) return;

        VehicleEntity vehicle = (VehicleEntity) ural;
        Vec3 eye = player.getEyePosition(1f);
        Vec3 end = eye.add(player.getViewVector(1f).scale(REACH));
        Matrix4d transform = VehicleVecUtils.INSTANCE.getVehicleYOffsetTransform(vehicle, 1f);
        AABB box = toWorldBox(transform, vehicle.getRotateOffsetHeight());

        Optional<Vec3> hit = box.clip(eye, end);
        if (hit.isEmpty()) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!player.level().isClientSide()) {
            ural.toggleTent();
            player.level().playSound(null, ural.blockPosition(),
                    ModSounds.SPRAY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.swing(event.getHand());
    }

    private static AABB toWorldBox(Matrix4d transform, double root) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            double x = (i & 1) == 0 ? MIN[0] : MAX[0];
            double y = (i & 2) == 0 ? MIN[1] : MAX[1];
            double z = (i & 4) == 0 ? MIN[2] : MAX[2];
            Vector4d w = transform.transform(new Vector4d(x, y - root, z, 1.0));
            minX = Math.min(minX, w.x); maxX = Math.max(maxX, w.x);
            minY = Math.min(minY, w.y); maxY = Math.max(maxY, w.y);
            minZ = Math.min(minZ, w.z); maxZ = Math.max(maxZ, w.z);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
