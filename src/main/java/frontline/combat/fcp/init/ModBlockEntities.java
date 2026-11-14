package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.block.entity.RearmStationBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
            DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, FCP.MODID);

    @SuppressWarnings("DataFlowIssue") // the data fixer type is unused for mod block entities
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RearmStationBlockEntity>> REARM_STATION = REGISTRY.register("rearm_station",
            () -> BlockEntityType.Builder.of(RearmStationBlockEntity::new, ModBlocks.REARM_STATION.get()).build(null));
}
