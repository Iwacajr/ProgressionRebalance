package com.iwaca.progressionrebalance;

import com.iwaca.progressionrebalance.config.ConfigManager;
import com.iwaca.progressionrebalance.enchantment.EnchantmentRebalance;
import com.iwaca.progressionrebalance.enchantment.WetLootCondition;
import com.iwaca.progressionrebalance.equipment.GoldEquipmentRebalance;
import com.iwaca.progressionrebalance.loot.LootModifiers;
import com.iwaca.progressionrebalance.loot.ModLootNumberProviders;
import com.iwaca.progressionrebalance.potion.ModPotions;
import com.iwaca.progressionrebalance.potion.PotionCauldrons;
import com.iwaca.progressionrebalance.potion.PotionStacking;
import com.iwaca.progressionrebalance.trade.DiamondGearTrades;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressionRebalance implements ModInitializer {
    public static final String MOD_ID = "progressionrebalance";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ConfigManager.get();

        ModPotions.register();
        ModLootNumberProviders.register();
        WetLootCondition.register();

        GoldEquipmentRebalance.register();
        PotionStacking.register();
        PotionCauldrons.register();
        LootModifiers.register();
        DiamondGearTrades.register();
        EnchantmentRebalance.register();
    }
}
