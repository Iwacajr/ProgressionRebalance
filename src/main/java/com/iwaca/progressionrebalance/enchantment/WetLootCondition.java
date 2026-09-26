package com.iwaca.progressionrebalance.enchantment;

import com.iwaca.progressionrebalance.ProgressionRebalance;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * Loot condition {@code progressionrebalance:is_wet}: the {@code this} entity is wet according to vanilla's
 * {@link Entity#isWet()}: touching water, standing in rain (open sky in a raining biome, so shelter and deserts
 * keep it dry) or inside a bubble column. Used by Impaling and available to data packs.
 */
public record WetLootCondition() implements LootCondition {
    public static final WetLootCondition INSTANCE = new WetLootCondition();
    public static final MapCodec<WetLootCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final LootConditionType TYPE = Registry.register(Registries.LOOT_CONDITION_TYPE, ProgressionRebalance.id("is_wet"), new LootConditionType(CODEC));

    /** Forces class initialization (and therefore registration) during mod initialization. */
    public static void register() {
        ProgressionRebalance.LOGGER.debug("Registered loot condition {}", Registries.LOOT_CONDITION_TYPE.getId(TYPE));
    }

    @Override
    public LootConditionType getType() {
        return TYPE;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Set.of(LootContextParameters.THIS_ENTITY);
    }

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.get(LootContextParameters.THIS_ENTITY);
        return entity != null && entity.isWet();
    }
}
