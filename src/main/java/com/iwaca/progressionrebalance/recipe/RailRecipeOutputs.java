package com.iwaca.progressionrebalance.recipe;

import com.iwaca.progressionrebalance.ProgressionRebalance;
import com.iwaca.progressionrebalance.config.ModConfig;
import com.iwaca.progressionrebalance.mixin.ShapedRecipeAccessor;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;

/**
 * Applies the configured output counts to the rail recipes after every recipe reload.
 *
 * <p>Only the result count is touched, and only if the recipe is still a shaped recipe producing the
 * expected item. If a data pack or another mod replaced one of these recipes with something different,
 * it is left alone.
 */
public final class RailRecipeOutputs {
    public static final Identifier COPPER_RAIL = ProgressionRebalance.id("rail_from_copper_ingot");
    public static final Identifier IRON_RAIL = Identifier.ofVanilla("rail");
    public static final Identifier POWERED_RAIL = Identifier.ofVanilla("powered_rail");

    private RailRecipeOutputs() {
    }

    public static void apply(Map<Identifier, RecipeEntry<?>> recipesById, ModConfig.Rails config) {
        setCount(recipesById.get(COPPER_RAIL), Items.RAIL, config.copperRailOutput());
        setCount(recipesById.get(IRON_RAIL), Items.RAIL, config.ironRailOutput());
        setCount(recipesById.get(POWERED_RAIL), Items.POWERED_RAIL, config.poweredRailOutput());
    }

    private static void setCount(RecipeEntry<?> entry, Item expectedResult, int count) {
        if (entry == null || !(entry.value() instanceof ShapedRecipe recipe)) {
            return;
        }
        ItemStack result = ((ShapedRecipeAccessor) recipe).progressionrebalance$getResult();
        if (!result.isOf(expectedResult)) {
            ProgressionRebalance.LOGGER.info("Recipe {} was replaced to produce {}; leaving its output unchanged",
                    entry.id(), result.getItem());
            return;
        }
        result.setCount(Math.min(count, result.getMaxCount()));
    }
}
