package com.iwaca.progressionrebalance.client.datagen;

import com.iwaca.progressionrebalance.potion.PotionCauldrons;
import com.iwaca.progressionrebalance.potion.PotionMixing;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

public class EnglishLanguageProvider extends FabricLanguageProvider {
    public EnglishLanguageProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder builder) {
        // Resistance potion (base name "progressionrebalance.resistance").
        builder.add("item.minecraft.potion.effect.progressionrebalance.resistance", "Potion of Resistance");
        builder.add("item.minecraft.splash_potion.effect.progressionrebalance.resistance", "Splash Potion of Resistance");
        builder.add("item.minecraft.lingering_potion.effect.progressionrebalance.resistance", "Lingering Potion of Resistance");
        builder.add("item.minecraft.tipped_arrow.effect.progressionrebalance.resistance", "Arrow of Resistance");

        builder.add("item.progressionrebalance.mixed_potion", "Mixed Potion");
        builder.add("item.progressionrebalance.mixed_splash_potion", "Mixed Splash Potion");
        builder.add("item.progressionrebalance.mixed_lingering_potion", "Mixed Lingering Potion");
        // Tipped arrows crafted from a mixed lingering potion only copy its effects, not its name, so they use
        // vanilla's name for arrows without a base potion ("Uncraftable Tipped Arrow").
        builder.add("item.minecraft.tipped_arrow.effect.empty", "Mixed Tipped Arrow");
        builder.add(PotionCauldrons.BLOCK, "Potion Cauldron");

        builder.add(PotionMixing.Rejection.NOTHING_TO_MIX.translationKey(), "That potion has no effect to mix in");
        builder.add(PotionMixing.Rejection.DIFFERENT_KINDS.translationKey(), "Only potions of the same kind (drinkable, splash or lingering) can be mixed");
        builder.add(PotionMixing.Rejection.NO_NEW_EFFECT.translationKey(), "That potion adds no new effect to the mixture");
        builder.add(PotionMixing.Rejection.TOO_MANY_EFFECTS.translationKey(), "A mixture can hold at most %s effects");

        // Keeps the id minecraft:bane_of_arthropods; only the name changes (its targets are #progressionrebalance:bane_targets).
        builder.add("enchantment.minecraft.bane_of_arthropods", "Bane");

        builder.add("container.progressionrebalance.enchant.page", "Offer page %s");
    }
}
