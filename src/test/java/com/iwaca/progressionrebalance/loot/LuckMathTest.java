package com.iwaca.progressionrebalance.loot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LuckMathTest {
    private static final float PER_LUCK = 0.5F;
    private static final float MAX_LUCK = 4.0F;

    @Test
    void noLuckMeansNoBonus() {
        assertEquals(0, LuckMath.bonusRolls(0.0F, PER_LUCK, MAX_LUCK, 0.0F));
        assertEquals(0, LuckMath.bonusRolls(-1.0F, PER_LUCK, MAX_LUCK, 0.0F));
        assertEquals(0, LuckMath.bonusRolls(3.0F, 0.0F, MAX_LUCK, 0.0F));
    }

    @Test
    void luckOneIsAFiftyPercentChanceOfOneRoll() {
        assertEquals(0.5, LuckMath.expectedBonusRolls(1.0F, PER_LUCK, MAX_LUCK));
        assertEquals(1, LuckMath.bonusRolls(1.0F, PER_LUCK, MAX_LUCK, 0.49F));
        assertEquals(0, LuckMath.bonusRolls(1.0F, PER_LUCK, MAX_LUCK, 0.5F));
    }

    @Test
    void higherLuckGivesMoreRolls() {
        assertEquals(1, LuckMath.bonusRolls(2.0F, PER_LUCK, MAX_LUCK, 0.99F));
        assertEquals(2, LuckMath.bonusRolls(3.0F, PER_LUCK, MAX_LUCK, 0.0F));
        assertEquals(1, LuckMath.bonusRolls(3.0F, PER_LUCK, MAX_LUCK, 0.99F));
    }

    @Test
    void luckIsCapped() {
        assertEquals(2, LuckMath.bonusRolls(4.0F, PER_LUCK, MAX_LUCK, 0.99F));
        assertEquals(2, LuckMath.bonusRolls(255.0F, PER_LUCK, MAX_LUCK, 0.0F));
    }

    @Test
    void averageMatchesTheExpectedValue() {
        int samples = 10_000;
        int total = 0;
        for (int i = 0; i < samples; i++) {
            total += LuckMath.bonusRolls(1.0F, PER_LUCK, MAX_LUCK, (i + 0.5F) / samples);
        }
        assertEquals(0.5, total / (double) samples, 1.0e-3);
    }
}
