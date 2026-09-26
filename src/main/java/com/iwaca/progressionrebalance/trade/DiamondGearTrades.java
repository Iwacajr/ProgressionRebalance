package com.iwaca.progressionrebalance.trade;

import com.iwaca.progressionrebalance.config.ConfigManager;
import com.iwaca.progressionrebalance.config.ModConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.Nullable;

/**
 * Villagers support diamond progression instead of replacing it: any armorer, toolsmith or weaponsmith trade
 * that sells diamond gear also asks for diamonds (by default half of the crafting cost, rounded up).
 *
 * <p>The trade still offers something crafting cannot (enchantments and a diamond discount), so trading halls
 * stay useful, but a player who has never mined a diamond can no longer buy a full diamond kit with emeralds.
 * This mirrors the direction of Mojang's own experimental villager trade rebalance, whose diamond armor trades
 * also take diamonds.
 *
 * <p>Implementation: the vanilla trade factories of those three professions are wrapped once at startup; the
 * wrapper post-processes each generated offer. No trade is added or removed, so levels keep the same number of
 * offers. Offers that already have a second input, or already cost diamonds, are left untouched. Villagers
 * that generated their trades before the mod was installed keep them.
 */
public final class DiamondGearTrades {
    private static final List<VillagerProfession> PROFESSIONS = List.of(
            VillagerProfession.ARMORER, VillagerProfession.TOOLSMITH, VillagerProfession.WEAPONSMITH);

    private DiamondGearTrades() {
    }

    public static void register() {
        ModConfig.Villagers config = ConfigManager.get().villagers();
        if (!config.enableTradeRebalance()) {
            return;
        }
        for (VillagerProfession profession : PROFESSIONS) {
            Int2ObjectMap<TradeOffers.Factory[]> levels = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(profession);
            if (levels == null) {
                continue;
            }
            Int2ObjectMap<TradeOffers.Factory[]> wrapped = new Int2ObjectOpenHashMap<>();
            for (Int2ObjectMap.Entry<TradeOffers.Factory[]> level : levels.int2ObjectEntrySet()) {
                wrapped.put(level.getIntKey(), Arrays.stream(level.getValue())
                        .map(factory -> new RequireDiamondsFactory(factory, config.diamondCostFraction()))
                        .toArray(TradeOffers.Factory[]::new));
            }
            levels.putAll(wrapped);
        }
    }

    /** Diamonds needed to craft the item, or 0 if it is not diamond equipment. */
    public static int craftingDiamonds(Item item) {
        if (item instanceof ArmorItem armor && armor.getMaterial().equals(ArmorMaterials.DIAMOND)) {
            return switch (armor.getType()) {
                case HELMET -> 5;
                case CHESTPLATE -> 8;
                case LEGGINGS -> 7;
                case BOOTS -> 4;
                case BODY -> 0;
            };
        }
        if (item instanceof ToolItem tool && tool.getMaterial() == ToolMaterials.DIAMOND) {
            if (item instanceof PickaxeItem || item instanceof AxeItem) {
                return 3;
            }
            if (item instanceof SwordItem || item instanceof HoeItem) {
                return 2;
            }
            if (item instanceof ShovelItem) {
                return 1;
            }
        }
        return 0;
    }

    /** Diamonds a trade asks for: a fraction of the crafting cost, rounded up, never less than one. */
    public static int requiredDiamonds(int craftingDiamonds, double fraction) {
        return Math.max(1, (int) Math.ceil(craftingDiamonds * fraction - 1.0e-9));
    }

    @Nullable
    static TradeOffer requireDiamonds(@Nullable TradeOffer offer, double fraction) {
        if (offer == null || offer.getSecondBuyItem().isPresent() || offer.getFirstBuyItem().item().value() == Items.DIAMOND) {
            return offer;
        }
        int craftingDiamonds = craftingDiamonds(offer.getSellItem().getItem());
        if (craftingDiamonds <= 0) {
            return offer;
        }
        TradedItem diamonds = new TradedItem(Items.DIAMOND, requiredDiamonds(craftingDiamonds, fraction));
        return new TradeOffer(offer.getFirstBuyItem(), Optional.of(diamonds), offer.getSellItem(), offer.getUses(),
                offer.getMaxUses(), offer.getMerchantExperience(), offer.getPriceMultiplier(), offer.getDemandBonus());
    }

    record RequireDiamondsFactory(TradeOffers.Factory delegate, double fraction) implements TradeOffers.Factory {
        @Nullable
        @Override
        public TradeOffer create(Entity entity, Random random) {
            return requireDiamonds(delegate.create(entity, random), fraction);
        }
    }
}
