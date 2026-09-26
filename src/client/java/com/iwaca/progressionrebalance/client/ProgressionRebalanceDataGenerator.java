package com.iwaca.progressionrebalance.client;

import com.iwaca.progressionrebalance.client.datagen.BlockTagProvider;
import com.iwaca.progressionrebalance.client.datagen.DamageTypeTagProvider;
import com.iwaca.progressionrebalance.client.datagen.EnglishLanguageProvider;
import com.iwaca.progressionrebalance.client.datagen.EntityTypeTagProvider;
import com.iwaca.progressionrebalance.client.datagen.PotionCauldronModelProvider;
import com.iwaca.progressionrebalance.client.datagen.RailRecipeProvider;
import com.iwaca.progressionrebalance.client.datagen.SpanishLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class ProgressionRebalanceDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(RailRecipeProvider::new);
        pack.addProvider(BlockTagProvider::new);
        pack.addProvider(EntityTypeTagProvider::new);
        pack.addProvider(DamageTypeTagProvider::new);
        pack.addProvider(PotionCauldronModelProvider::new);
        pack.addProvider(EnglishLanguageProvider::new);
        pack.addProvider(SpanishLanguageProvider::new);
    }
}
