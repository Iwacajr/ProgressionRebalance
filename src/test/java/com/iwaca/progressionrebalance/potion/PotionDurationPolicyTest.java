package com.iwaca.progressionrebalance.potion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.iwaca.progressionrebalance.config.ModConfig;
import com.iwaca.progressionrebalance.potion.PotionDurationPolicy.Category;
import java.util.List;
import java.util.Set;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potions;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PotionDurationPolicyTest {
    private static final Set<Identifier> COMBAT = Set.of(Identifier.ofVanilla("strength"), Identifier.ofVanilla("regeneration"),
            Identifier.ofVanilla("resistance"));

    @BeforeAll
    static void bootstrap() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void categoriesArePrioritised() {
        assertEquals(Category.UNCHANGED, PotionDurationPolicy.categorize(false, true, true, true));
        assertEquals(Category.AMPLIFIED, PotionDurationPolicy.categorize(true, true, true, false));
        assertEquals(Category.COMBAT, PotionDurationPolicy.categorize(true, false, true, false));
        assertEquals(Category.HARMFUL, PotionDurationPolicy.categorize(true, false, false, true));
        assertEquals(Category.UTILITY, PotionDurationPolicy.categorize(true, false, false, false));
    }

    @Test
    void vanillaPotionsAreClassifiedAsDesigned() {
        assertEquals(Category.UTILITY, categoryOf(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 3600)));
        assertEquals(Category.UTILITY, categoryOf(new StatusEffectInstance(StatusEffects.LUCK, 6000)));
        assertEquals(Category.COMBAT, categoryOf(new StatusEffectInstance(StatusEffects.STRENGTH, 3600)));
        assertEquals(Category.AMPLIFIED, categoryOf(new StatusEffectInstance(StatusEffects.STRENGTH, 1800, 1)));
        assertEquals(Category.HARMFUL, categoryOf(new StatusEffectInstance(StatusEffects.POISON, 900)));
        assertEquals(Category.UNCHANGED, categoryOf(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1)));
        // Turtle Master: Slowness IV + Resistance III is a strong potion and keeps vanilla duration.
        assertEquals(Category.AMPLIFIED, PotionDurationPolicy.categorize(List.of(
                new StatusEffectInstance(StatusEffects.SLOWNESS, 400, 3),
                new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 2)), COMBAT));
    }

    @Test
    void registeredVanillaPotionsUseTheDefaultMultipliers() {
        // The PotionsMixin has already rebalanced the registry during bootstrap.
        ModConfig.Potions defaults = ModConfig.defaults().potions();
        assertEquals(PotionDurationPolicy.scaleDuration(3600, defaults.utilityDurationMultiplier()), durationOf(Potions.NIGHT_VISION.value().getEffects()));
        assertEquals(PotionDurationPolicy.scaleDuration(3600, defaults.combatDurationMultiplier()), durationOf(Potions.STRENGTH.value().getEffects()));
        assertEquals(1800, durationOf(Potions.STRONG_STRENGTH.value().getEffects()));
        assertEquals(900, durationOf(Potions.POISON.value().getEffects()));
    }

    @Test
    void scalingRounds() {
        assertEquals(1350, PotionDurationPolicy.scaleDuration(900, 1.5));
        assertEquals(1, PotionDurationPolicy.scaleDuration(1, 0.1));
    }

    private static Category categoryOf(StatusEffectInstance effect) {
        return PotionDurationPolicy.categorize(List.of(effect), COMBAT);
    }

    private static int durationOf(List<StatusEffectInstance> effects) {
        return effects.getFirst().getDuration();
    }
}
