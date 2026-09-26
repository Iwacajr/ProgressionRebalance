package com.iwaca.progressionrebalance.client.datagen;

import com.iwaca.progressionrebalance.potion.PotionCauldrons;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.BlockStateVariantMap;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.data.client.VariantsBlockStateSupplier;
import net.minecraft.util.Identifier;

/** The potion cauldron reuses the vanilla water cauldron models; the client tints their liquid. */
public class PotionCauldronModelProvider extends FabricModelProvider {
    public PotionCauldronModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(PotionCauldrons.BLOCK)
                .coordinate(BlockStateVariantMap.create(LeveledCauldronBlock.LEVEL)
                        .register(1, model("block/water_cauldron_level1"))
                        .register(2, model("block/water_cauldron_level2"))
                        .register(3, model("block/water_cauldron_full"))));
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
    }

    private static BlockStateVariant model(String path) {
        return BlockStateVariant.create().put(VariantSettings.MODEL, Identifier.ofVanilla(path));
    }
}
