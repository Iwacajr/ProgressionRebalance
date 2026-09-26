package com.iwaca.progressionrebalance.config;

import java.util.List;
import java.util.Properties;

/**
 * Every balance value that server owners are realistically expected to tune.
 *
 * <p>This class is deliberately free of Minecraft types so it can be parsed and unit tested in isolation.
 * Defaults here are the single source of truth for the mod's balance numbers.
 */
public record ModConfig(
        Transport transport,
        Rails rails,
        Gold gold,
        Enchanting enchanting,
        Enchantments enchantments,
        Potions potions,
        Luck luck,
        Villagers villagers,
        Loot loot
) {
    public record Transport(double minecartSpeedMultiplier) {
    }

    public record Rails(int copperRailOutput, int ironRailOutput, int poweredRailOutput) {
    }

    public record Gold(
            boolean enabled,
            int toolDurability,
            boolean ironMiningTier,
            int swordDurability,
            double swordAttackDamage,
            double swordAttackSpeed,
            double axeAttackDamage,
            double axeAttackSpeed,
            int armorDurabilityMultiplier,
            int helmetArmor,
            int chestplateArmor,
            int leggingsArmor,
            int bootsArmor
    ) {
    }

    public record Enchanting(boolean lapisPages, int maxPages) {
        /** Page count that is actually in effect, taking the feature toggle into account. */
        public int effectiveMaxPages() {
            return lapisPages ? maxPages : 1;
        }
    }

    public record Enchantments(double protectionReductionPerLevel, double thornsDamageMultiplier, double impalingWetMultiplier) {
    }

    public record Potions(
            double utilityDurationMultiplier,
            double combatDurationMultiplier,
            double amplifiedDurationMultiplier,
            double harmfulDurationMultiplier,
            List<String> combatEffects,
            boolean mixingEnabled,
            int maxMixedEffects,
            int drinkableStackSize
    ) {
    }

    public record Luck(
            boolean enabled,
            double chestBonusRollChancePerLuck,
            double mobDropBonusRollChancePerLuck,
            double maxEffectiveLuck
    ) {
    }

    public record Villagers(boolean enableTradeRebalance, double diamondCostFraction) {
    }

    public record Loot(boolean enableStructureLootRebalance) {
    }

    public static ModConfig defaults() {
        return read(new ConfigReader(new Properties()));
    }

    public static ModConfig read(ConfigReader reader) {
        reader.section("Transport");
        Transport transport = new Transport(
                reader.getDouble("transport.minecartSpeedMultiplier", 2.5, 1.0, 2.5,
                        "Multiplies the top speed of minecarts on straight rails and slopes. Vanilla powered travel",
                        "is 8 blocks/s, so 2.5 gives 20 blocks/s. Capped at 2.5 (one block per tick): faster carts",
                        "would skip rail blocks. Curves keep vanilla speed so carts cannot derail there; momentum is",
                        "kept through the curve. Off-rail movement is not affected.")
        );

        reader.section("Rails", "Crafting output of rail recipes. Applied whenever recipes are (re)loaded.");
        Rails rails = new Rails(
                reader.getInt("rails.copperRailOutput", 8, 1, 64,
                        "Rails produced by the copper rail recipe (6 copper ingots + 1 stick)."),
                reader.getInt("rails.ironRailOutput", 16, 1, 64,
                        "Rails produced by the vanilla iron rail recipe (vanilla: 16)."),
                reader.getInt("rails.poweredRailOutput", 12, 1, 64,
                        "Powered rails produced by the vanilla powered rail recipe (vanilla: 6).")
        );

        reader.section("Gold", "Gold is a fast, highly enchantable, high-performance but fragile equipment tier.",
                "Its armor protects better than iron but lacks diamond's toughness and durability.",
                "Only vanilla gold items (and items built on the vanilla gold materials) are affected.");
        Gold gold = new Gold(
                reader.getBoolean("gold.enabled", true,
                        "Master switch for the gold equipment changes below."),
                reader.getInt("gold.toolDurability", 128, 1, 2031,
                        "Durability of golden pickaxes, axes, shovels and hoes (vanilla: 32, stone: 131, iron: 250)."),
                reader.getBoolean("gold.ironMiningTier", true,
                        "Lets golden tools harvest everything an iron tool can (iron, gold, redstone, diamond ore).",
                        "Obsidian and ancient debris still require diamond."),
                reader.getInt("gold.swordDurability", 180, 1, 2031,
                        "Durability of golden swords (vanilla: 32, iron: 250, diamond: 1561). 180 is 72% of iron."),
                reader.getDouble("gold.swordAttackDamage", 6.5, 1.0, 20.0,
                        "Attack damage of the golden sword, as shown in its tooltip (vanilla gold: 4, iron: 6, diamond: 7)."),
                reader.getDouble("gold.swordAttackSpeed", 1.8, 0.5, 4.0,
                        "Attack speed of the golden sword, as shown in its tooltip (vanilla swords: 1.6).",
                        "1.8 recharges a full-strength hit in 11 ticks instead of 12 (about 9% more full hits)."),
                reader.getDouble("gold.axeAttackDamage", 8.0, 1.0, 20.0,
                        "Attack damage of the golden axe (vanilla gold: 7, iron: 9, diamond: 9)."),
                reader.getDouble("gold.axeAttackSpeed", 1.1, 0.5, 4.0,
                        "Attack speed of the golden axe (vanilla gold: 1.0, iron: 0.9, diamond: 1.0)."),
                reader.getInt("gold.armorDurabilityMultiplier", 11, 1, 37,
                        "Armor durability multiplier for golden armor (vanilla gold: 7, chainmail/iron: 15, diamond: 33).",
                        "Helmet/chestplate/leggings/boots durability is 11/16/15/13 times this value. 11 is 73% of iron."),
                reader.getInt("gold.helmetArmor", 3, 0, 10,
                        "Armor points of the golden helmet (vanilla gold: 2, iron: 2, diamond: 3)."),
                reader.getInt("gold.chestplateArmor", 7, 0, 10,
                        "Armor points of the golden chestplate (vanilla gold: 5, iron: 6, diamond: 8)."),
                reader.getInt("gold.leggingsArmor", 5, 0, 10,
                        "Armor points of the golden leggings (vanilla gold: 3, iron: 5, diamond: 6)."),
                reader.getInt("gold.bootsArmor", 3, 0, 10,
                        "Armor points of the golden boots (vanilla gold: 1, iron: 2, diamond: 3).",
                        "Gold keeps no armor toughness and no knockback resistance: diamond still takes big hits better.")
        );

        reader.section("Enchanting", "Lapis pages: the amount of lapis in the table selects a page of three offers.");
        Enchanting enchanting = new Enchanting(
                reader.getBoolean("enchanting.lapisPages", true,
                        "1-3 lapis shows page 1 (costs 1/2/3), 4-6 shows page 2 (costs 4/5/6), and so on."),
                reader.getInt("enchanting.maxPages", 3, 1, 21,
                        "Number of offer pages. Extra lapis beyond the last page keeps showing the last page.")
        );

        reader.section("Enchantments", "General-purpose enchantments stay convenient; specialized ones are stronger where they apply.",
                "Only the vanilla (or mod-bundled) definitions are changed: enchantments replaced by a data pack are left alone.");
        Enchantments enchantments = new Enchantments(
                reader.getDouble("enchantments.protectionReductionPerLevel", 3.0, 0.0, 4.0,
                        "Damage reduction in percent per level of Protection on each armor piece (vanilla: 4.0).",
                        "3.0 makes full Protection IV 48% instead of 64%. Fire, Blast and Projectile Protection keep",
                        "8% per level against their damage, and the vanilla 80% total cap still applies."),
                reader.getDouble("enchantments.thornsDamageMultiplier", 1.35, 0.0, 10.0,
                        "Multiplies the damage Thorns reflects (vanilla: 1 to 5). Chance and armor wear are unchanged.",
                        "Thorns damage never uses up the attacker's damage cooldown, whatever this value is."),
                reader.getDouble("enchantments.impalingWetMultiplier", 0.6, 0.0, 1.0,
                        "Share of the Impaling bonus that wet (in water or rain) non-aquatic targets take.",
                        "Aquatic targets always take the full bonus. 0 restores vanilla Impaling.")
        );

        reader.section("Potions");
        Potions potions = new Potions(
                reader.getDouble("potions.utilityDurationMultiplier", 2.0, 0.1, 10.0,
                        "Duration multiplier for brewed utility potions (night vision, swiftness, fire resistance, ...)."),
                reader.getDouble("potions.combatDurationMultiplier", 1.5, 0.1, 10.0,
                        "Duration multiplier for brewed potions containing a combat effect (see combatEffects)."),
                reader.getDouble("potions.amplifiedDurationMultiplier", 1.0, 0.1, 10.0,
                        "Duration multiplier for level II+ brewed potions. Takes priority over the other categories."),
                reader.getDouble("potions.harmfulDurationMultiplier", 1.0, 0.1, 10.0,
                        "Duration multiplier for brewed potions whose effects are all harmful (poison, slowness, ...)."),
                reader.getList("potions.combatEffects", List.of(
                                "minecraft:strength", "minecraft:regeneration", "minecraft:resistance",
                                "minecraft:absorption", "minecraft:health_boost"),
                        "Status effects that mark a potion as a combat potion. Unknown ids are ignored."),
                reader.getBoolean("potions.mixingEnabled", true,
                        "Allow pouring potions into a water cauldron to mix them. An empty bottle takes the whole",
                        "mixture out as one potion and uses one water level."),
                reader.getInt("potions.maxMixedEffects", 3, 2, 8,
                        "Maximum number of distinct effects a mixed potion may hold."),
                reader.getInt("potions.drinkableStackSize", 4, 1, 16,
                        "Maximum stack size of drinkable potions (vanilla: 1). Splash and lingering potions never stack.")
        );

        reader.section("Luck", "Luck (from the Luck effect or the luck attribute) adds bonus loot rolls.");
        Luck luck = new Luck(
                reader.getBoolean("luck.enabled", true,
                        "Master switch for the Luck loot integration."),
                reader.getDouble("luck.chestBonusRollChancePerLuck", 0.5, 0.0, 4.0,
                        "Expected extra rolls of each structure chest loot pool per point of luck.",
                        "0.5 means Luck I gives each pool a 50% chance of one extra roll."),
                reader.getDouble("luck.mobDropBonusRollChancePerLuck", 0.5, 0.0, 4.0,
                        "Same as above, for rare (chance-based, player-kill) mob drop pools."),
                reader.getDouble("luck.maxEffectiveLuck", 4.0, 0.0, 64.0,
                        "Luck above this value gives no further loot bonus.")
        );

        reader.section("Villagers");
        Villagers villagers = new Villagers(
                reader.getBoolean("villagers.enableTradeRebalance", true,
                        "Armorer, toolsmith and weaponsmith trades that sell diamond gear also require diamonds."),
                reader.getDouble("villagers.diamondCostFraction", 0.5, 0.1, 1.0,
                        "Fraction of the item's crafting diamonds that the trade requires (rounded up, minimum 1).")
        );

        reader.section("Loot");
        Loot loot = new Loot(
                reader.getBoolean("loot.enableStructureLootRebalance", true,
                        "Add thematic supplies and potions to vanilla structure chests, and a rare brewed potion drop",
                        "to witches killed by a player. Additive: never removes vanilla loot.")
        );

        return new ModConfig(transport, rails, gold, enchanting, enchantments, potions, luck, villagers, loot);
    }
}
