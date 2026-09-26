package com.iwaca.progressionrebalance.client.datagen;

import com.iwaca.progressionrebalance.config.ModConfig;
import com.iwaca.progressionrebalance.recipe.RailRecipeOutputs;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

/**
 * Generates the copper rail recipe. It mirrors the vanilla rail layout and accepts any conventional copper
 * ingot. The output count written here is the config default; the configured value is applied at load time
 * by {@link RailRecipeOutputs}, which also handles the vanilla rail and powered rail recipes without
 * overriding their JSON files.
 */
public class RailRecipeProvider extends FabricRecipeProvider {
    public RailRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, Items.RAIL, ModConfig.defaults().rails().copperRailOutput())
                .pattern("X X")
                .pattern("X#X")
                .pattern("X X")
                .input('X', ConventionalItemTags.COPPER_INGOTS)
                .input('#', Items.STICK)
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter, RailRecipeOutputs.COPPER_RAIL);
    }
}
