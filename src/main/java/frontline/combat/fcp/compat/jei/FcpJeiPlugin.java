package frontline.combat.fcp.compat.jei;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.init.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Only loaded by JEI. Hides FCP's admin-only blocks from the ingredient list. */
@JeiPlugin
public class FcpJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(FCP.MODID, "jei");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                List.of(new ItemStack(ModBlocks.REARM_STATION.get())));
    }
}
