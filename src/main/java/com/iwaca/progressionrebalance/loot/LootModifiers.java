package com.iwaca.progressionrebalance.loot;

import com.iwaca.progressionrebalance.config.ConfigManager;
import com.iwaca.progressionrebalance.config.ModConfig;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

/**
 * Entry point for all loot table changes. Only built-in tables (vanilla or mod provided) are modified;
 * a table that a data pack replaces is left exactly as the data pack defines it.
 */
public final class LootModifiers {
    private LootModifiers() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }
            ModConfig config = ConfigManager.get();
            // Structure additions first, so the new pools also receive lucky copies.
            if (config.loot().enableStructureLootRebalance()) {
                StructureLoot.modify(key, table, registries);
            }
            if (config.luck().enabled()) {
                LuckLoot.modify(key, table, config.luck());
            }
        });
    }
}
