package com.iwaca.progressionrebalance.enchantment;

import net.minecraft.enchantment.EnchantmentLevelBasedValue;

/** Arithmetic on the level-based values used inside vanilla enchantment definitions. */
public final class EnchantmentValues {
    private EnchantmentValues() {
    }

    /**
     * Returns a value that is {@code factor} times {@code value} at every level. Constant and linear values
     * (all that vanilla uses for the changed enchantments) stay constant and linear, so the result reads like
     * the vanilla data; anything else is wrapped in a fraction.
     */
    public static EnchantmentLevelBasedValue scale(EnchantmentLevelBasedValue value, float factor) {
        if (value instanceof EnchantmentLevelBasedValue.Constant constant) {
            return EnchantmentLevelBasedValue.constant(constant.value() * factor);
        }
        if (value instanceof EnchantmentLevelBasedValue.Linear linear) {
            return EnchantmentLevelBasedValue.linear(linear.base() * factor, linear.perLevelAboveFirst() * factor);
        }
        if (factor == 0.0F) {
            return EnchantmentLevelBasedValue.constant(0.0F);
        }
        return new EnchantmentLevelBasedValue.Fraction(value, EnchantmentLevelBasedValue.constant(1.0F / factor));
    }
}
