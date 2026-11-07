package frontline.combat.fcp.entity.vehicle;

import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.tools.OBB;
import frontline.combat.fcp.team.VehicleTeamLock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import org.joml.Vector3d;

/**
 * FCP land vehicles that can't float (Buoyancy 0) flood their engine once the water is over the hull; wading up to the roof is
 * fine. With the engine dead, the vehicle loses a tenth of its max health every second it stays under. Back on land that stops
 * and SBW's passive part repair brings the engine back; SBW's own burning still takes over if health has fallen to 10%.
 */
public final class EngineFlooding {

    /** Fraction of the engine's health lost per tick, on top of what SBW's passive repair gives back: about 5 seconds. */
    private static final float DRAIN = 1f / 100f;
    /** SBW's passive part repair per tick (VehicleEffectUtils), which flooding has to outrun. */
    private static final float REPAIR = 0.0025f;
    /** Squad's water damage on the M1A2 and T-72B3: 300 of 3000 HP a hit. The interval isn't in the SDK's assets, so it's a guess. */
    private static final float HIT = 0.1f;
    private static final int HIT_INTERVAL = 20;

    private EngineFlooding() {
    }

    public static void tick(VehicleEntity vehicle) {
        if (vehicle.level().isClientSide() || vehicle.isWreck() || !VehicleTeamLock.isFcpVehicle(vehicle)) return;
        EngineInfo engine = vehicle.getEngineInfo();
        if (!(engine instanceof EngineInfo.Track || engine instanceof EngineInfo.Wheel) || engine.getBuoyancy() != 0) return;
        if (!hullSubmerged(vehicle)) return;
        float max = vehicle.getMainEngineMaxHealth();
        vehicle.setMainEngineHealth(Math.max(0f, vehicle.getMainEngineHealth() - max * (DRAIN + REPAIR)));
        if (vehicle.getMainEngineHealth() > 0f) return;
        if (vehicle.tickCount % HIT_INTERVAL == 0) {
            vehicle.onHurt(HIT * vehicle.getMaxHealth(), vehicle.getLastAttacker(), false);
        }
    }

    private static boolean hullSubmerged(VehicleEntity vehicle) {
        OBB box = vehicle.getCollisionOBB();
        if (box == null) return vehicle.isUnderWater();
        Vector3d c = box.center;
        BlockPos roof = BlockPos.containing(c.x, c.y + box.extents().y, c.z);
        return vehicle.level().getFluidState(roof).is(FluidTags.WATER);
    }
}
