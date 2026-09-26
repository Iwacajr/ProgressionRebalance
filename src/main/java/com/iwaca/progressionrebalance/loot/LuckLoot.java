package com.iwaca.progressionrebalance.loot;

import com.iwaca.progressionrebalance.config.ModConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.KilledByPlayerLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.condition.RandomChanceWithEnchantedBonusLootCondition;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

/**
 * Makes Luck matter for vanilla loot by giving eligible pools a "lucky copy".
 *
 * <p>A lucky copy is an exact duplicate of a pool (same entries, conditions and functions) whose roll count
 * comes from {@link LuckBonusRollsProvider}. With no luck it never rolls, so unlucky players see pure
 * vanilla loot. Because the copy re-evaluates the original pool's conditions, a 2.5% rare drop stays a
 * chance-based rare drop: Luck improves the odds without ever guaranteeing it.
 *
 * <p>Scope is explicit and limited to vanilla ({@code minecraft:}) tables:
 * <ul>
 *     <li>structure chests (not the spawn bonus chest, the jungle temple arrow dispenser or trial chamber
 *     tables, whose vault rewards reference nested tables and already have their own key economy);</li>
 *     <li>rare mob drops: entity pools gated by both a player kill and a random chance.</li>
 * </ul>
 * Fishing is not touched because vanilla already feeds luck into fishing entry quality. Archaeology is not
 * touched because brushable blocks keep only the first generated item, so bonus rolls would be discarded.
 */
public final class LuckLoot {
    private LuckLoot() {
    }

    static void modify(RegistryKey<LootTable> key, LootTable.Builder table, ModConfig.Luck config) {
        Identifier id = key.getValue();
        if (!id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            return;
        }

        Predicate<LootPool> eligible;
        double rollsPerLuck;
        if (isLuckyChest(id.getPath())) {
            eligible = pool -> true;
            rollsPerLuck = config.chestBonusRollChancePerLuck();
        } else if (id.getPath().startsWith("entities/")) {
            eligible = LuckLoot::isRareDropPool;
            rollsPerLuck = config.mobDropBonusRollChancePerLuck();
        } else {
            return;
        }
        if (rollsPerLuck <= 0.0) {
            return;
        }

        LuckBonusRollsProvider rolls = new LuckBonusRollsProvider((float) rollsPerLuck, (float) config.maxEffectiveLuck());
        List<LootPool> luckyCopies = new ArrayList<>();
        table.modifyPools(builder -> {
            LootPool pool = builder.build();
            if (eligible.test(pool)) {
                luckyCopies.add(FabricLootPoolBuilder.copyOf(pool)
                        .rolls(rolls)
                        .bonusRolls(ConstantLootNumberProvider.create(0.0F))
                        .build());
            }
        });
        table.pools(luckyCopies);
    }

    static boolean isLuckyChest(String path) {
        return path.startsWith("chests/")
                && !path.startsWith("chests/trial_chambers/")
                && !path.equals("chests/spawn_bonus_chest")
                && !path.equals("chests/jungle_temple_dispenser");
    }

    static boolean isRareDropPool(LootPool pool) {
        boolean playerKill = false;
        boolean chance = false;
        for (LootCondition condition : pool.conditions) {
            playerKill |= condition instanceof KilledByPlayerLootCondition;
            chance |= condition instanceof RandomChanceLootCondition || condition instanceof RandomChanceWithEnchantedBonusLootCondition;
        }
        return playerKill && chance;
    }
}
