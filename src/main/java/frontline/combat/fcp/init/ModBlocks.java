package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.block.HescoBlock;
import frontline.combat.fcp.block.RearmStationBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> REGISTRY =
            DeferredRegister.create(ForgeRegistries.BLOCKS, FCP.MODID);

    /** Hardness 2 (a little over stone's 1.5), blast resistance 12 (stone 6, obsidian 1200). */
    private static final float HESCO_HARDNESS = 2.0f, HESCO_BLAST_RESISTANCE = 12.0f;

    public static final RegistryObject<Block> HESCO_SAND = register("hesco_sand",
            () -> new HescoBlock(0xDBD3A0, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(HESCO_HARDNESS, HESCO_BLAST_RESISTANCE)
                    .sound(SoundType.SAND)));

    public static final RegistryObject<Block> HESCO_DIRT = register("hesco_dirt",
            () -> new HescoBlock(0x866043, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(HESCO_HARDNESS, HESCO_BLAST_RESISTANCE)
                    .sound(SoundType.GRAVEL)));

    /** Hidden admin block: kept out of the creative tabs and JEI (FcpJeiPlugin). */
    public static final RegistryObject<Block> REARM_STATION = register("rearm_station",
            () -> new RearmStationBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0f, 1200.0f)
                    .sound(SoundType.METAL)));

    /** Registers the block and its item. */
    private static RegistryObject<Block> register(String name, Supplier<Block> block) {
        RegistryObject<Block> registered = REGISTRY.register(name, block);
        ModItems.REGISTRY.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }
}
