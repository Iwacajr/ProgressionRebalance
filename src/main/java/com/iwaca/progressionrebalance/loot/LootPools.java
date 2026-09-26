package com.iwaca.progressionrebalance.loot;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.EmptyEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.function.SetPotionLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;

/** Small builders that keep the loot definitions in {@link StructureLoot} readable. */
final class LootPools {
    private LootPools() {
    }

    /** A single-roll pool; include {@link #nothing(int)} to make the pool a chance rather than a guarantee. */
    static LootPool.Builder oneOf(LeafEntry.Builder<?>... entries) {
        LootPool.Builder pool = LootPool.builder().rolls(ConstantLootNumberProvider.create(1));
        for (LeafEntry.Builder<?> entry : entries) {
            pool.with(entry);
        }
        return pool;
    }

    static LeafEntry.Builder<?> potion(RegistryEntry<Potion> potion, int weight) {
        return ItemEntry.builder(Items.POTION).weight(weight).apply(SetPotionLootFunction.builder(potion));
    }

    static LeafEntry.Builder<?> item(ItemConvertible item, int weight) {
        return ItemEntry.builder(item).weight(weight);
    }

    static LeafEntry.Builder<?> item(ItemConvertible item, int weight, int min, int max) {
        return ItemEntry.builder(item).weight(weight)
                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(min, max)));
    }

    static LeafEntry.Builder<?> nothing(int weight) {
        return EmptyEntry.builder().weight(weight);
    }
}
