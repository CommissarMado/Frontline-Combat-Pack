package frontline.combat.fcp.init;

import net.minecraft.world.food.FoodProperties;

public class ModFoods {
    public static final FoodProperties DELICIOUS_SNACK = new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).alwaysEdible().build();
    public static final FoodProperties REDBULL = new FoodProperties.Builder().nutrition(1).saturationModifier(0.1F).alwaysEdible().build();
}
