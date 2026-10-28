package frontline.combat.fcp.block.entity;

import com.atsuishio.superbwarfare.block.entity.ChargingStationBlockEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import frontline.combat.fcp.balance.VehicleRearm;
import frontline.combat.fcp.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Services every vehicle within SBW's charging-station radius once a second. */
public class RearmStationBlockEntity extends BlockEntity {

    private static final int INTERVAL = 20;

    public RearmStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REARM_STATION.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RearmStationBlockEntity station) {
        if (level.getGameTime() % INTERVAL != 0) return;
        AABB range = new AABB(pos).inflate(ChargingStationBlockEntity.CHARGE_RADIUS);
        for (VehicleEntity vehicle : level.getEntitiesOfClass(VehicleEntity.class, range)) {
            if (!vehicle.isWreck()) VehicleRearm.service(vehicle, INTERVAL);
        }
    }
}
