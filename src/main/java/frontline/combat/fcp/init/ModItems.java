package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;

public class ModItems {
    public static final DeferredRegister<Item> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ITEMS, FCP.MODID);

    public static final RegistryObject<Item> TERRORIST_TAB_ICON = REGISTRY.register("terrorist_tab_icon", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RUSSIAN_TAB_ICON = REGISTRY.register("russian_tab_icon", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> AMERICAN_TAB_ICON = REGISTRY.register("american_tab_icon", () -> new Item(new Item.Properties()));

    // Animated creative-tab icon (cycles the 3 old tab icons via an animated texture)
    public static final RegistryObject<Item> FC_TAB_ICON = REGISTRY.register("fc_tab_icon", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> DELICIOUS_SNACK = REGISTRY.register("delicious_snack",
            () -> new Item(new Item.Properties().food(ModFoods.DELICIOUS_SNACK)));
    public static final RegistryObject<Item> REDBULL = REGISTRY.register("redbull",
            () -> new Item(new Item.Properties().food(ModFoods.REDBULL).stacksTo(1)));

    public static final RegistryObject<Item> SPRAY = REGISTRY.register("spray",
            () -> new frontline.combat.fcp.item.varies.SprayItem());
}
