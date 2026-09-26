package com.iwaca.progressionrebalance.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.enchantment.effect.value.AddEnchantmentEffect;
import net.minecraft.entity.DamageUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EnchantmentRebalanceTest {
    private static final float EPSILON = 1.0e-4F;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    /** Vanilla Protection: linear(1, 1) protection points. (Minecraft classes may only load after bootstrap.) */
    private static EnchantmentLevelBasedValue vanillaProtection() {
        return EnchantmentLevelBasedValue.linear(1.0F);
    }

    /** Vanilla Fire, Blast and Projectile Protection: linear(2, 2) points against their damage. */
    private static EnchantmentLevelBasedValue specializedProtection() {
        return EnchantmentLevelBasedValue.linear(2.0F);
    }

    @Test
    void constantAndLinearValuesStayReadable() {
        assertEquals(EnchantmentLevelBasedValue.constant(6.75F), EnchantmentValues.scale(EnchantmentLevelBasedValue.constant(5.0F), 1.35F));
        EnchantmentLevelBasedValue impaling = EnchantmentValues.scale(EnchantmentLevelBasedValue.linear(2.5F), 0.6F);
        assertInstanceOf(EnchantmentLevelBasedValue.Linear.class, impaling);
        assertEquals(7.5F, impaling.getValue(5), EPSILON, "Impaling V on wet targets: 60% of +12.5");
    }

    @Test
    void otherValuesAreScaledAtEveryLevel() {
        EnchantmentLevelBasedValue squared = new EnchantmentLevelBasedValue.LevelsSquared(1.0F);
        EnchantmentLevelBasedValue half = EnchantmentValues.scale(squared, 0.5F);
        for (int level = 1; level <= 5; level++) {
            assertEquals(squared.getValue(level) * 0.5F, half.getValue(level), EPSILON);
        }
        assertEquals(0.0F, EnchantmentValues.scale(squared, 0.0F).getValue(3));
    }

    @Test
    void protectionEntriesAreScaledAndKeepTheirRequirements() {
        List<EnchantmentEffectEntry<EnchantmentValueEffect>> entries = new ArrayList<>(List.of(
                new EnchantmentEffectEntry<>(new AddEnchantmentEffect(vanillaProtection()), Optional.empty())));
        EnchantmentRebalance.scaleProtection(entries, EnchantmentRebalance.protectionFactor(3.0));
        AddEnchantmentEffect scaled = assertInstanceOf(AddEnchantmentEffect.class, entries.getFirst().effect());
        assertEquals(3.0F, scaled.value().getValue(4), EPSILON, "Protection IV: 3 points = 12%");
        assertEquals(Optional.empty(), entries.getFirst().requirements());
        assertEquals(1.0F, EnchantmentRebalance.protectionFactor(4.0), "4% per level is vanilla");
    }

    /** The target numbers of the design, through vanilla's own formula (4% per point, capped at 20 points). */
    @Test
    void protectionCombinationsMatchTheDesign() {
        float general = EnchantmentValues.scale(vanillaProtection(), EnchantmentRebalance.protectionFactor(3.0)).getValue(4);
        float specialized = specializedProtection().getValue(4);

        assertEquals(0.48F, reduction(4 * general), EPSILON, "4x Protection IV");
        assertEquals(0.36F, reduction(3 * general), EPSILON, "3x Protection IV, general damage");
        assertEquals(0.68F, reduction(3 * general + specialized), EPSILON, "3x Protection IV + 1 specialized, matching damage");
        assertEquals(0.24F, reduction(2 * general), EPSILON, "2x Protection IV, general damage");
        assertEquals(0.80F, reduction(2 * general + 2 * specialized), EPSILON, "2+2 reaches the vanilla cap");
        assertEquals(0.80F, reduction(4 * specialized), EPSILON, "the cap still applies to 4 specialized pieces");
        assertEquals(0.64F, reduction(4 * vanillaProtection().getValue(4)), EPSILON, "vanilla full Protection IV, for comparison");
    }

    private static float reduction(float protectionPoints) {
        return 1.0F - DamageUtil.getInflictedDamage(1.0F, protectionPoints);
    }
}
