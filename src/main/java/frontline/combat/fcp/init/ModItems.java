package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.bus.api.IEventBus;

public class ModItems {
    public static final DeferredRegister<Item> REGISTRY =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM, FCP.MODID);

    public static final DeferredHolder<Item, Item> TERRORIST_TAB_ICON = REGISTRY.register("terrorist_tab_icon", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> RUSSIAN_TAB_ICON = REGISTRY.register("russian_tab_icon", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> AMERICAN_TAB_ICON = REGISTRY.register("american_tab_icon", () -> new Item(new Item.Properties()));

    // Animated creative-tab icon (cycles the 3 old tab icons via an animated texture)
    public static final DeferredHolder<Item, Item> FC_TAB_ICON = REGISTRY.register("fc_tab_icon", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DELICIOUS_SNACK = REGISTRY.register("delicious_snack",
            () -> new Item(new Item.Properties().food(ModFoods.DELICIOUS_SNACK)));
    public static final DeferredHolder<Item, Item> REDBULL = REGISTRY.register("redbull",
            () -> new Item(new Item.Properties().food(ModFoods.REDBULL).stacksTo(1)));

    /** Squad-style AT mine (FcpMineEntity). */
    public static final DeferredHolder<Item, Item> MINE = REGISTRY.register("mine", frontline.combat.fcp.item.FcpMineItem::new);

    public static final DeferredHolder<Item, Item> SPRAY = REGISTRY.register("spray",
            () -> new frontline.combat.fcp.item.varies.SprayItem());

    /** Placeholder: use on an Abrams or Leopard 2 to fit a camo net and cycle its variants (VehicleCamoNet). */
    public static final DeferredHolder<Item, Item> CAMO_NET = REGISTRY.register("camo_net",
            () -> new frontline.combat.fcp.item.varies.CamoNetItem());
}
