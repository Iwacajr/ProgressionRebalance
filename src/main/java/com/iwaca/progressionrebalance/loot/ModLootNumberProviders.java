package com.iwaca.progressionrebalance.loot;

import com.iwaca.progressionrebalance.ProgressionRebalance;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModLootNumberProviders {
    public static final LootNumberProviderType LUCK_BONUS_ROLLS = new LootNumberProviderType(LuckBonusRollsProvider.CODEC);

    private ModLootNumberProviders() {
    }

    public static void register() {
        Registry.register(Registries.LOOT_NUMBER_PROVIDER_TYPE, ProgressionRebalance.id("luck_bonus_rolls"), LUCK_BONUS_ROLLS);
    }
}
