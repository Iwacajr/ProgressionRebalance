package com.iwaca.progressionrebalance.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;

/**
 * Loot number provider {@code progressionrebalance:luck_bonus_rolls}: a roll count driven by the luck of
 * the loot context (the Luck effect / luck attribute of the player who opened the chest or got the kill).
 *
 * <p>It is registered with a codec, so data packs can use it in their own pools, for example
 * {@code "rolls": {"type": "progressionrebalance:luck_bonus_rolls", "rolls_per_luck": 0.5}}.
 */
public record LuckBonusRollsProvider(float rollsPerLuck, float maxLuck) implements LootNumberProvider {
    public static final float DEFAULT_MAX_LUCK = 4.0F;

    public static final MapCodec<LuckBonusRollsProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("rolls_per_luck").forGetter(LuckBonusRollsProvider::rollsPerLuck),
            Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("max_luck", DEFAULT_MAX_LUCK).forGetter(LuckBonusRollsProvider::maxLuck)
    ).apply(instance, LuckBonusRollsProvider::new));

    @Override
    public float nextFloat(LootContext context) {
        return nextInt(context);
    }

    @Override
    public int nextInt(LootContext context) {
        // Without luck, return before touching the random source so seeded loot stays exactly as without the mod.
        if (LuckMath.expectedBonusRolls(context.getLuck(), rollsPerLuck, maxLuck) <= 0.0) {
            return 0;
        }
        return LuckMath.bonusRolls(context.getLuck(), rollsPerLuck, maxLuck, context.getRandom().nextFloat());
    }

    @Override
    public LootNumberProviderType getType() {
        return ModLootNumberProviders.LUCK_BONUS_ROLLS;
    }
}
