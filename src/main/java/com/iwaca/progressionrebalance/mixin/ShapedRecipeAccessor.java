package com.iwaca.progressionrebalance.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the result stack of a shaped recipe so its count can be adjusted after loading
 * (see {@link com.iwaca.progressionrebalance.recipe.RailRecipeOutputs}).
 */
@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccessor {
    @Accessor("result")
    ItemStack progressionrebalance$getResult();
}
