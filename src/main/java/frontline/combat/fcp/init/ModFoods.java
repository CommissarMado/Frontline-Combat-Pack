package frontline.combat.fcp.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoods {
    public static final FoodProperties DELICIOUS_SNACK = new FoodProperties.Builder().alwaysEdible().nutrition(5)
            .saturationModifier(0.6f).build();

    public static final FoodProperties REDBULL = new FoodProperties.Builder().alwaysEdible().nutrition(5)
            .saturationModifier(0.6f).effect(() -> new MobEffectInstance(MobEffects.LEVITATION, 60, 0), 1.0f)  .build();
}
