package com.iwaca.progressionrebalance.client.datagen;

import com.iwaca.progressionrebalance.potion.PotionCauldrons;
import com.iwaca.progressionrebalance.potion.PotionMixing;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

/** Spanish (Spain). Keys match {@link EnglishLanguageProvider}. */
public class SpanishLanguageProvider extends FabricLanguageProvider {
    public SpanishLanguageProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "es_es", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder builder) {
        builder.add("item.minecraft.potion.effect.progressionrebalance.resistance", "Poción de resistencia");
        builder.add("item.minecraft.splash_potion.effect.progressionrebalance.resistance", "Poción arrojadiza de resistencia");
        builder.add("item.minecraft.lingering_potion.effect.progressionrebalance.resistance", "Poción persistente de resistencia");
        builder.add("item.minecraft.tipped_arrow.effect.progressionrebalance.resistance", "Flecha de resistencia");

        builder.add("item.progressionrebalance.mixed_potion", "Poción mezclada");
        builder.add("item.progressionrebalance.mixed_splash_potion", "Poción arrojadiza mezclada");
        builder.add("item.progressionrebalance.mixed_lingering_potion", "Poción persistente mezclada");
        builder.add("item.minecraft.tipped_arrow.effect.empty", "Flecha con efectos mezclados");
        builder.add(PotionCauldrons.BLOCK, "Caldero con poción");

        builder.add(PotionMixing.Rejection.NOTHING_TO_MIX.translationKey(), "Esa poción no tiene ningún efecto que mezclar");
        builder.add(PotionMixing.Rejection.DIFFERENT_KINDS.translationKey(), "Solo se pueden mezclar pociones del mismo tipo (bebible, arrojadiza o persistente)");
        builder.add(PotionMixing.Rejection.NO_NEW_EFFECT.translationKey(), "Esa poción no añade ningún efecto nuevo a la mezcla");
        builder.add(PotionMixing.Rejection.TOO_MANY_EFFECTS.translationKey(), "Una mezcla puede tener como máximo %s efectos");

        builder.add("enchantment.minecraft.bane_of_arthropods", "Perdición");

        builder.add("container.progressionrebalance.enchant.page", "Página de ofertas %s");
    }
}
