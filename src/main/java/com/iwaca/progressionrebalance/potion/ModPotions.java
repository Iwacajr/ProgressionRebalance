package com.iwaca.progressionrebalance.potion;

import com.iwaca.progressionrebalance.ProgressionRebalance;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * Vanilla has a Resistance status effect but no Resistance potion. This single potion type (not a new item;
 * it uses the vanilla potion items) makes Resistance a rare exploration reward. It has no brewing recipe.
 */
public final class ModPotions {
    /** 3:00 of Resistance I. */
    private static final int RESISTANCE_DURATION_TICKS = 3 * 60 * 20;

    public static final RegistryEntry<Potion> RESISTANCE = Registry.registerReference(
            Registries.POTION,
            ProgressionRebalance.id("resistance"),
            // The base name produces translation keys such as item.minecraft.potion.effect.progressionrebalance.resistance
            new Potion(ProgressionRebalance.MOD_ID + ".resistance", new StatusEffectInstance(StatusEffects.RESISTANCE, RESISTANCE_DURATION_TICKS)));

    private ModPotions() {
    }

    /** Forces class initialization (and therefore registration) during mod initialization. */
    public static void register() {
        ProgressionRebalance.LOGGER.debug("Registered potion {}", RESISTANCE.getIdAsString());
    }
}
