package com.iwaca.progressionrebalance.potion;

import com.iwaca.progressionrebalance.config.ModConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

/**
 * Data-driven duration policy for brewed (vanilla) potions.
 *
 * <p>Each potion is put in exactly one category, checked in this order:
 * <ol>
 *     <li>{@link Category#UNCHANGED}: no timed effect at all (water, awkward, instant health/damage);</li>
 *     <li>{@link Category#AMPLIFIED}: any effect at level II or higher, e.g. Strength II or Turtle Master.
 *     Strong potions are the most dangerous to lengthen, so by default they keep vanilla durations;</li>
 *     <li>{@link Category#COMBAT}: contains a configured combat effect (Strength, Regeneration, ...);</li>
 *     <li>{@link Category#HARMFUL}: every effect is harmful (Poison, Slowness, Weakness, ...). These are
 *     mostly thrown at others, so lengthening them would mainly change PvP;</li>
 *     <li>{@link Category#UTILITY}: everything else (Night Vision, Fire Resistance, Swiftness, Luck, ...).</li>
 * </ol>
 * The whole potion is scaled by one multiplier so that multi-effect potions keep their internal balance.
 */
public final class PotionDurationPolicy {
    public enum Category {
        UNCHANGED, AMPLIFIED, COMBAT, HARMFUL, UTILITY
    }

    private PotionDurationPolicy() {
    }

    public static Category categorize(boolean hasTimedEffect, boolean anyAmplified, boolean anyCombat, boolean allHarmful) {
        if (!hasTimedEffect) {
            return Category.UNCHANGED;
        }
        if (anyAmplified) {
            return Category.AMPLIFIED;
        }
        if (anyCombat) {
            return Category.COMBAT;
        }
        return allHarmful ? Category.HARMFUL : Category.UTILITY;
    }

    public static double multiplier(Category category, ModConfig.Potions config) {
        return switch (category) {
            case UNCHANGED -> 1.0;
            case AMPLIFIED -> config.amplifiedDurationMultiplier();
            case COMBAT -> config.combatDurationMultiplier();
            case HARMFUL -> config.harmfulDurationMultiplier();
            case UTILITY -> config.utilityDurationMultiplier();
        };
    }

    public static int scaleDuration(int ticks, double multiplier) {
        return Math.max(1, (int) Math.round(ticks * multiplier));
    }

    public static Category categorize(List<StatusEffectInstance> effects, Set<Identifier> combatEffects) {
        boolean hasTimedEffect = false;
        boolean anyAmplified = false;
        boolean anyCombat = false;
        boolean allHarmful = true;
        for (StatusEffectInstance effect : effects) {
            if (!DurationContributions.isTimed(effect)) {
                continue;
            }
            hasTimedEffect = true;
            anyAmplified |= effect.getAmplifier() > 0;
            anyCombat |= effect.getEffectType().getKey().map(key -> combatEffects.contains(key.getValue())).orElse(false);
            allHarmful &= effect.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL;
        }
        return categorize(hasTimedEffect, anyAmplified, anyCombat, allHarmful);
    }

    /** Returns the rebalanced effect list, or the original list if nothing changes. */
    public static List<StatusEffectInstance> rebalance(List<StatusEffectInstance> effects, ModConfig.Potions config) {
        double multiplier = multiplier(categorize(effects, parseIds(config.combatEffects())), config);
        if (multiplier == 1.0) {
            return effects;
        }
        List<StatusEffectInstance> scaled = new ArrayList<>(effects.size());
        for (StatusEffectInstance effect : effects) {
            scaled.add(DurationContributions.isTimed(effect) ? withDuration(effect, scaleDuration(effect.getDuration(), multiplier)) : effect);
        }
        return List.copyOf(scaled);
    }

    private static StatusEffectInstance withDuration(StatusEffectInstance effect, int duration) {
        RegistryEntry<StatusEffect> type = effect.getEffectType();
        return new StatusEffectInstance(type, duration, effect.getAmplifier(), effect.isAmbient(),
                effect.shouldShowParticles(), effect.shouldShowIcon());
    }

    private static Set<Identifier> parseIds(List<String> ids) {
        return ids.stream().map(Identifier::tryParse).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
    }
}
