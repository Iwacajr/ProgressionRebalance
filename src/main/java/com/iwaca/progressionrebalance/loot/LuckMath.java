package com.iwaca.progressionrebalance.loot;

/**
 * Pure luck arithmetic, kept separate from Minecraft types so it can be unit tested.
 *
 * <p>Expected bonus rolls are {@code min(luck, maxLuck) * rollsPerLuck}. The fractional part is resolved
 * randomly, so 0.5 expected rolls means a 50% chance of one roll. This keeps Luck I meaningful even with
 * small per-luck values (vanilla's {@code bonus_rolls} floors the product, which makes low values useless).
 */
public final class LuckMath {
    private LuckMath() {
    }

    public static double expectedBonusRolls(float luck, float rollsPerLuck, float maxLuck) {
        if (luck <= 0.0F || rollsPerLuck <= 0.0F) {
            return 0.0;
        }
        return Math.min(luck, maxLuck) * (double) rollsPerLuck;
    }

    /**
     * @param randomValue a uniformly distributed value in {@code [0, 1)}
     */
    public static int bonusRolls(float luck, float rollsPerLuck, float maxLuck, float randomValue) {
        double expected = expectedBonusRolls(luck, rollsPerLuck, maxLuck);
        int whole = (int) Math.floor(expected);
        return whole + (randomValue < expected - whole ? 1 : 0);
    }
}
