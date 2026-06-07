package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(Registries.ITEM, FCP.MODID);

    public static final Supplier<Item> TERRORIST_TAB_ICON = REGISTRY.register("terrorist_tab_icon", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> RUSSIAN_TAB_ICON = REGISTRY.register("russian_tab_icon", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> AMERICAN_TAB_ICON = REGISTRY.register("american_tab_icon", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> DELICIOUS_SNACK = REGISTRY.register("delicious_snack", () -> new Item(new Item.Properties().food(ModFoods.DELICIOUS_SNACK)));
    public static final Supplier<Item> REDBULL = REGISTRY.register("redbull", () -> new Item(new Item.Properties().food(ModFoods.REDBULL).stacksTo(1)));
    public static final Supplier<Item> SPRAY = REGISTRY.register("spray", () -> new frontline.combat.fcp.item.varies.SprayItem());
}
