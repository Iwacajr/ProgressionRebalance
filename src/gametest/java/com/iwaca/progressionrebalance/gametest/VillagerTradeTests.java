package com.iwaca.progressionrebalance.gametest;

import com.iwaca.progressionrebalance.trade.DiamondGearTrades;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;

public class VillagerTradeTests implements FabricGameTest {
    private static final int OFFERS_PER_FACTORY = 20;

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void diamondGearTradesRequireDiamonds(TestContext context) {
        VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, 1, 1, 1);
        Random random = Random.create(42L);
        int diamondGearOffers = 0;
        for (VillagerProfession profession : List.of(VillagerProfession.ARMORER, VillagerProfession.TOOLSMITH, VillagerProfession.WEAPONSMITH)) {
            Int2ObjectMap<TradeOffers.Factory[]> levels = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(profession);
            for (TradeOffers.Factory[] factories : levels.values()) {
                for (TradeOffers.Factory factory : factories) {
                    for (int i = 0; i < OFFERS_PER_FACTORY; i++) {
                        TradeOffer offer = factory.create(villager, random);
                        if (offer != null) {
                            diamondGearOffers += check(context, profession, offer);
                        }
                    }
                }
            }
        }
        context.assertTrue(diamondGearOffers > 0, "no diamond gear offers were generated");
        context.complete();
    }

    /** Returns 1 if the offer sells diamond gear, after validating it. */
    private static int check(TestContext context, VillagerProfession profession, TradeOffer offer) {
        int craftingDiamonds = DiamondGearTrades.craftingDiamonds(offer.getSellItem().getItem());
        if (craftingDiamonds == 0) {
            context.assertFalse(offer.getSecondBuyItem().map(item -> item.item().value() == Items.DIAMOND).orElse(false),
                    profession + " added diamonds to a non-diamond trade: " + offer.getSellItem());
            return 0;
        }
        TradedItem second = offer.getSecondBuyItem().orElse(null);
        context.assertTrue(second != null && second.item().value() == Items.DIAMOND
                        && second.count() == DiamondGearTrades.requiredDiamonds(craftingDiamonds, 0.5),
                profession + " sells " + offer.getSellItem() + " without the expected diamonds: " + second);
        context.assertTrue(offer.getFirstBuyItem().item().value() == Items.EMERALD, "emerald price is kept");
        return 1;
    }
}
