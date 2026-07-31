package frontline.combat.fcp.entity.vehicle.Kamaz;

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

/** Toggle the tent (pehota2 / pehota4) on the Kamaz and Kamaz Long with the spray + look, like the Ural. */
@Mod.EventBusSubscriber(modid = FCP.MODID)
public final class KamazTentHandler {

    private static final double REACH = 5.0;
    private static final double[] TENT = {-1.6292, 1.0268, -13.7643, 1.6335, 3.1423, 1.1424};
    private static final double[] TENT2 = {-1.6292, 1.0268, -14.2941, 1.6342, 3.1423, 2.4135};

    private KamazTentHandler() {}

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() != ModItems.SPRAY.get()) return;

        VehicleEntity vehicle;
        double[] box;
        Runnable toggle;
        if (event.getTarget() instanceof KamazEntity k) {
            vehicle = k; box = TENT; toggle = k::toggleTent;
        } else if (event.getTarget() instanceof KamazLongEntity k) {
            vehicle = k; box = TENT2; toggle = k::toggleTent;
        } else {
            return;
        }

        Vec3 eye = player.getEyePosition(1f);
        Vec3 end = eye.add(player.getViewVector(1f).scale(REACH));
        Matrix4d transform = VehicleVecUtils.INSTANCE.getVehicleYOffsetTransform(vehicle, 1f);
        AABB world = toWorldBox(box, transform, vehicle.getRotateOffsetHeight());
        if (world.clip(eye, end).isEmpty()) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!player.level().isClientSide()) {
            toggle.run();
            player.level().playSound(null, vehicle.blockPosition(),
                    ModSounds.SPRAY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.swing(event.getHand());
    }

    private static AABB toWorldBox(double[] b, Matrix4d transform, double root) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            double x = (i & 1) == 0 ? b[0] : b[3];
            double y = (i & 2) == 0 ? b[1] : b[4];
            double z = (i & 4) == 0 ? b[2] : b[5];
            Vector4d w = transform.transform(new Vector4d(x, y - root, z, 1.0));
            minX = Math.min(minX, w.x); maxX = Math.max(maxX, w.x);
            minY = Math.min(minY, w.y); maxY = Math.max(maxY, w.y);
            minZ = Math.min(minZ, w.z); maxZ = Math.max(maxZ, w.z);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
