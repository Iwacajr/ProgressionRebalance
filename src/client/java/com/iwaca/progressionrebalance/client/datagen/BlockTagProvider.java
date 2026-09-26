package com.iwaca.progressionrebalance.client.datagen;

import com.iwaca.progressionrebalance.potion.PotionCauldrons;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

public class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public BlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        // #cauldrons is also part of #mineable/pickaxe, so the potion cauldron is mined like any cauldron.
        getOrCreateTagBuilder(BlockTags.CAULDRONS).add(PotionCauldrons.BLOCK);
    }
}
