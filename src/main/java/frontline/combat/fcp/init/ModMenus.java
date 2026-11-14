package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.menu.VehicleInventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

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
            DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, FCP.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<VehicleInventoryMenu>> VEHICLE_INVENTORY =
            MENUS.register("vehicle_inventory",
                    () -> IMenuTypeExtension.create(VehicleInventoryMenu::fromNetwork));
}