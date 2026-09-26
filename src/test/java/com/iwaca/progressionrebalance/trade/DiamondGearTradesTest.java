package com.iwaca.progressionrebalance.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DiamondGearTradesTest {
    @Test
    void halfTheCraftingCostRoundedUp() {
        assertEquals(4, DiamondGearTrades.requiredDiamonds(8, 0.5)); // chestplate
        assertEquals(4, DiamondGearTrades.requiredDiamonds(7, 0.5)); // leggings
        assertEquals(3, DiamondGearTrades.requiredDiamonds(5, 0.5)); // helmet
        assertEquals(2, DiamondGearTrades.requiredDiamonds(4, 0.5)); // boots
        assertEquals(2, DiamondGearTrades.requiredDiamonds(3, 0.5)); // pickaxe, axe
        assertEquals(1, DiamondGearTrades.requiredDiamonds(2, 0.5)); // sword, hoe
        assertEquals(1, DiamondGearTrades.requiredDiamonds(1, 0.5)); // shovel
    }

    @Test
    void neverLessThanOneAndNeverMoreThanCrafting() {
        assertEquals(1, DiamondGearTrades.requiredDiamonds(8, 0.1));
        assertEquals(8, DiamondGearTrades.requiredDiamonds(8, 1.0));
        assertEquals(3, DiamondGearTrades.requiredDiamonds(3, 1.0));
    }
}
