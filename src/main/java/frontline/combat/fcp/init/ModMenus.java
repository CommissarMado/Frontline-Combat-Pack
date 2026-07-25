package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.menu.VehicleInventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * FCP menu types.
 *
 * One menu type covers every vehicle hold regardless of size or filter, so adding cargo to
 * a new vehicle never touches this class.
 *
 * NOTE: like ModTabs, this DeferredRegister must be attached to the mod event bus in the
 * FCP main class constructor:
 *     ModMenus.MENUS.register(modEventBus);
 */
public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, FCP.MODID);

    public static final RegistryObject<MenuType<VehicleInventoryMenu>> VEHICLE_INVENTORY =
            MENUS.register("vehicle_inventory",
                    () -> IForgeMenuType.create(VehicleInventoryMenu::fromNetwork));
}