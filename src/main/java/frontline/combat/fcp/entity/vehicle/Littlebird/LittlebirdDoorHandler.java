package frontline.combat.fcp.entity.vehicle.Littlebird;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.init.ModItems;
import frontline.combat.fcp.init.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
 * Toggles the unarmed Littlebird's "door toggle" bone (which parents both side doors at once) the
 * same way the UAZ-3303's tent works: right-click the helicopter with the FCP spray while looking
 * at either door's hitbox. Whichever box the ray actually hits (nearest to the player) triggers
 * the single shared toggle; if the ray misses both, the event is left alone so the spray's default
 * camo repaint still works.
 */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class LittlebirdDoorHandler {

    private static final double REACH = 5.0;

    // "door" (right side) hitbox, min/max in SuperbWarfare's vehicle-local space (derived from
    // littlebird.geo.json's "door" bone, same rotation-aware bbox-of-cubes method used for OH-1).
    private static final double[] MIN_DOOR = {0.6764, 0.7167, -0.4950};
    private static final double[] MAX_DOOR = {0.8667, 2.1095, 0.7072};

    // "door2" (left side) hitbox.
    private static final double[] MIN_DOOR2 = {-0.8594, 0.7167, -0.4950};
    private static final double[] MAX_DOOR2 = {-0.6692, 2.1095, 0.7072};

    private LittlebirdDoorHandler() {}

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() != ModItems.SPRAY.get()) return;
        if (!(event.getTarget() instanceof LittlebirdEntity heli)) return;

        VehicleEntity vehicle = heli;
        Vec3 eye = player.getEyePosition(1f);
        Vec3 end = eye.add(player.getViewVector(1f).scale(REACH));
        Matrix4d transform = VehicleVecUtils.INSTANCE.getVehicleYOffsetTransform(vehicle, 1f);
        double root = vehicle.getRotateOffsetHeight();

        AABB doorBox = toWorldBox(MIN_DOOR, MAX_DOOR, transform, root);
        AABB door2Box = toWorldBox(MIN_DOOR2, MAX_DOOR2, transform, root);

        Optional<Vec3> hitDoor = doorBox.clip(eye, end);
        Optional<Vec3> hitDoor2 = door2Box.clip(eye, end);

        if (hitDoor.isEmpty() && hitDoor2.isEmpty()) return; // neither box hit: leave the event alone

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!player.level().isClientSide()) {
            heli.toggleDoors();
            player.level().playSound(null, heli.blockPosition(),
                    ModSounds.SPRAY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.swing(event.getHand());
    }

    private static AABB toWorldBox(double[] min, double[] max, Matrix4d transform, double root) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            double x = (i & 1) == 0 ? min[0] : max[0];
            double y = (i & 2) == 0 ? min[1] : max[1];
            double z = (i & 4) == 0 ? min[2] : max[2];
            Vector4d w = transform.transform(new Vector4d(x, y - root, z, 1.0));
            minX = Math.min(minX, w.x); maxX = Math.max(maxX, w.x);
            minY = Math.min(minY, w.y); maxY = Math.max(maxY, w.y);
            minZ = Math.min(minZ, w.z); maxZ = Math.max(maxZ, w.z);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
