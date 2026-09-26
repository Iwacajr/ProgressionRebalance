package com.iwaca.progressionrebalance.loot;

import static com.iwaca.progressionrebalance.loot.LootPools.item;
import static com.iwaca.progressionrebalance.loot.LootPools.nothing;
import static com.iwaca.progressionrebalance.loot.LootPools.oneOf;
import static com.iwaca.progressionrebalance.loot.LootPools.potion;

import com.iwaca.progressionrebalance.potion.ModPotions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.KilledByPlayerLootCondition;
import net.minecraft.loot.condition.RandomChanceWithEnchantedBonusLootCondition;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;

/**
 * Additive, thematic loot for vanilla structures.
 *
 * <p>Every addition is a new pool appended to the vanilla table; nothing vanilla is removed. Each pool is a
 * single roll that includes an explicit "nothing" weight, so the listed weights read directly as odds
 * per chest. Potions are deliberately uncommon: finding one should feel like a useful bonus, not a
 * guaranteed supply. No structure is required for progression; they only become more worth visiting.
 */
public final class StructureLoot {
    /** Base chance (plus 2% per Looting level) that a witch killed by a player drops a brewed potion. */
    private static final float WITCH_POTION_CHANCE = 0.08F;
    private static final float WITCH_POTION_CHANCE_PER_LOOTING = 0.02F;

    private static final Map<RegistryKey<LootTable>, Function<RegistryWrapper.WrapperLookup, List<LootPool.Builder>>> ADDITIONS = new HashMap<>();

    static {
        // Mineshafts: mining supplies and a light source for the dark.
        add(LootTables.ABANDONED_MINESHAFT_CHEST,
                oneOf(item(Items.MINECART, 2), item(Items.LANTERN, 3, 1, 3), nothing(5)),
                oneOf(potion(Potions.NIGHT_VISION, 3), nothing(7)));

        // Nether fortress: the brewing outpost of the Nether.
        add(LootTables.NETHER_BRIDGE_CHEST,
                oneOf(item(Items.BLAZE_POWDER, 3, 1, 3), item(Items.MAGMA_CREAM, 2, 1, 2),
                        item(Items.GLOWSTONE_DUST, 2, 2, 5), nothing(3)),
                oneOf(potion(Potions.FIRE_RESISTANCE, 4), potion(Potions.LONG_FIRE_RESISTANCE, 1),
                        potion(Potions.STRENGTH, 2), nothing(8)));

        // Bastions: stronger Nether combat rewards, concentrated in the treasure room.
        add(LootTables.BASTION_TREASURE_CHEST,
                oneOf(potion(Potions.LONG_FIRE_RESISTANCE, 2), potion(Potions.STRONG_STRENGTH, 2),
                        potion(Potions.LONG_REGENERATION, 1), potion(ModPotions.RESISTANCE, 1), nothing(4)));
        for (RegistryKey<LootTable> bastion : List.of(LootTables.BASTION_OTHER_CHEST,
                LootTables.BASTION_BRIDGE_CHEST, LootTables.BASTION_HOGLIN_STABLE_CHEST)) {
            add(bastion, oneOf(potion(Potions.FIRE_RESISTANCE, 2), potion(Potions.STRENGTH, 1), nothing(9)));
        }

        // Ruined portals: a hint of preparation for the Nether.
        add(LootTables.RUINED_PORTAL_CHEST, oneOf(potion(Potions.FIRE_RESISTANCE, 2), nothing(8)));

        // Shipwrecks and ocean ruins: aquatic utility and exploration tools.
        add(LootTables.SHIPWRECK_SUPPLY_CHEST,
                oneOf(potion(Potions.WATER_BREATHING, 3), potion(Potions.NIGHT_VISION, 2), nothing(5)),
                oneOf(item(Items.SPYGLASS, 1), item(Items.COMPASS, 1), nothing(8)));
        add(LootTables.SHIPWRECK_TREASURE_CHEST,
                oneOf(potion(Potions.LONG_WATER_BREATHING, 2), potion(Potions.LUCK, 1), nothing(7)));
        add(LootTables.UNDERWATER_RUIN_SMALL_CHEST, oneOf(potion(Potions.WATER_BREATHING, 2), nothing(8)));
        add(LootTables.UNDERWATER_RUIN_BIG_CHEST,
                oneOf(potion(Potions.WATER_BREATHING, 2), potion(Potions.LONG_WATER_BREATHING, 1),
                        potion(Potions.NIGHT_VISION, 1), nothing(6)));
        add(LootTables.BURIED_TREASURE_CHEST,
                oneOf(potion(Potions.LONG_WATER_BREATHING, 2), potion(Potions.LUCK, 1), nothing(7)));

        // Woodland mansions: rare combat and alchemy utility.
        add(LootTables.WOODLAND_MANSION_CHEST,
                oneOf(potion(Potions.LONG_INVISIBILITY, 2), potion(Potions.STRENGTH, 1),
                        potion(Potions.REGENERATION, 1), nothing(10)));

        // Ancient cities: darkness, stealth and enchanting.
        add(LootTables.ANCIENT_CITY_CHEST,
                oneOf(potion(Potions.LONG_NIGHT_VISION, 2), potion(Potions.INVISIBILITY, 1),
                        potion(ModPotions.RESISTANCE, 1), nothing(10)),
                oneOf(item(Items.LAPIS_LAZULI, 3, 4, 12), item(Items.EXPERIENCE_BOTTLE, 2, 2, 5), nothing(5)));

        // End cities: endgame mobility, plus the rarest route to Luck.
        add(LootTables.END_CITY_TREASURE_CHEST,
                oneOf(potion(Potions.LONG_SLOW_FALLING, 3), potion(Potions.LUCK, 1), nothing(6)));

        // Smaller early-game structures: occasional utility potions.
        add(LootTables.SIMPLE_DUNGEON_CHEST,
                oneOf(potion(Potions.SWIFTNESS, 1), potion(Potions.NIGHT_VISION, 1),
                        potion(Potions.REGENERATION, 1), nothing(9)));
        add(LootTables.JUNGLE_TEMPLE_CHEST,
                oneOf(potion(Potions.LEAPING, 1), potion(Potions.INVISIBILITY, 1), nothing(8)));
        add(LootTables.DESERT_PYRAMID_CHEST,
                oneOf(potion(Potions.FIRE_RESISTANCE, 1), potion(Potions.SWIFTNESS, 1), nothing(8)));
        for (RegistryKey<LootTable> stronghold : List.of(LootTables.STRONGHOLD_CORRIDOR_CHEST,
                LootTables.STRONGHOLD_CROSSING_CHEST)) {
            add(stronghold, oneOf(potion(Potions.SLOW_FALLING, 1), potion(Potions.NIGHT_VISION, 1), nothing(8)));
        }

        // Swamp huts have no chest in Java Edition, so the witch herself carries the alchemy loot.
        ADDITIONS.put(EntityType.WITCH.getLootTableId(), registries -> List.of(
                oneOf(potion(Potions.FIRE_RESISTANCE, 1), potion(Potions.WATER_BREATHING, 1),
                        potion(Potions.SWIFTNESS, 1), potion(Potions.HEALING, 1), potion(Potions.NIGHT_VISION, 1),
                        potion(Potions.INVISIBILITY, 1), potion(Potions.SLOW_FALLING, 1),
                        potion(Potions.REGENERATION, 1))
                        .conditionally(KilledByPlayerLootCondition.builder())
                        .conditionally(RandomChanceWithEnchantedBonusLootCondition.builder(
                                registries, WITCH_POTION_CHANCE, WITCH_POTION_CHANCE_PER_LOOTING))));
    }

    private StructureLoot() {
    }

    private static void add(RegistryKey<LootTable> table, LootPool.Builder... pools) {
        List<LootPool.Builder> list = List.of(pools);
        ADDITIONS.put(table, registries -> list);
    }

    static void modify(RegistryKey<LootTable> key, LootTable.Builder table, RegistryWrapper.WrapperLookup registries) {
        Function<RegistryWrapper.WrapperLookup, List<LootPool.Builder>> additions = ADDITIONS.get(key);
        if (additions != null) {
            for (LootPool.Builder pool : additions.apply(registries)) {
                table.pool(pool.build());
            }
        }
    }
}
