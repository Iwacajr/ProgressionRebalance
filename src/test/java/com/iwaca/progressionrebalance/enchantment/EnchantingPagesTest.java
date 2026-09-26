package com.iwaca.progressionrebalance.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class EnchantingPagesTest {
    @Test
    void lapisSelectsPagesOfThree() {
        int[] expectedPage = {0, 0, 0, 0, 1, 1, 1, 2, 2, 2};
        for (int lapis = 0; lapis < expectedPage.length; lapis++) {
            assertEquals(expectedPage[lapis], EnchantingPages.pageFor(lapis, 3), "lapis " + lapis);
        }
    }

    @Test
    void extraLapisStaysOnTheLastPage() {
        assertEquals(2, EnchantingPages.pageFor(10, 3));
        assertEquals(2, EnchantingPages.pageFor(64, 3));
        assertEquals(20, EnchantingPages.pageFor(64, 21));
    }

    @Test
    void singlePageIsVanilla() {
        for (int lapis = 0; lapis <= 64; lapis++) {
            assertEquals(0, EnchantingPages.pageFor(lapis, 1));
        }
    }

    @Test
    void costsFollowThePage() {
        assertEquals(1, EnchantingPages.lapisCost(0, 0));
        assertEquals(3, EnchantingPages.lapisCost(0, 2));
        assertEquals(4, EnchantingPages.lapisCost(1, 0));
        assertEquals(6, EnchantingPages.lapisCost(1, 2));
        assertEquals(7, EnchantingPages.lapisCost(2, 0));
        assertEquals(9, EnchantingPages.lapisCost(2, 2));
    }

    @Test
    void everyAffordableOfferOnAPageIsReachableWithThatPagesLapis() {
        for (int lapis = 1; lapis <= 9; lapis++) {
            int page = EnchantingPages.pageFor(lapis, 3);
            // The cheapest offer of the selected page is always affordable.
            assertEquals(true, EnchantingPages.lapisCost(page, 0) <= lapis);
        }
    }

    @Test
    void pageZeroKeepsTheVanillaSeedAndOtherPagesDiffer() {
        long seed = 123456789L;
        assertEquals(seed, EnchantingPages.pageSeed(seed, 0));
        assertNotEquals(EnchantingPages.pageSeed(seed, 1), EnchantingPages.pageSeed(seed, 2));
        // Vanilla uses seed + slot for the three offers; page seeds must not collide with those.
        for (int slot = 0; slot < 3; slot++) {
            assertNotEquals(seed + slot, EnchantingPages.pageSeed(seed, 1));
        }
    }
}
