# Balance Summary

Default values. Details: [BALANCE.md](BALANCE.md). Update this file whenever an implemented rebalance changes.

## Equipment
- Gold Tool Durability (pickaxe, axe, shovel, hoe): 32 → 128
- Gold Tool Harvest Tier: Wood → Iron
- Gold Mining Speed: 12 → 12
- Golden Sword Damage: 4 → 6.5
- Golden Sword Attack Speed: 1.6 → 1.8
- Golden Sword Durability: 32 → 180
- Golden Axe Damage: 7 → 8
- Golden Axe Attack Speed: 1.0 → 1.1
- Gold Armor Points (helmet/chest/legs/boots): 2/5/3/1 → 3/7/5/3
- Gold Armor Total: 11 → 18
- Gold Armor Toughness: 0 → 0
- Gold Armor Durability Multiplier: ×7 → ×11 (77/112/105/91 → 121/176/165/143)
- Gold Enchantability (tools/armor): 22/25 → 22/25

## Enchantments
- Protection: 4% → 3% per level/piece
- Full Protection IV: 64% → 48%
- Fire/Blast/Projectile Protection: 8% → 8% per level/piece
- Protection Cap: 80% → 80%
- Bane of Arthropods → Bane
- Bane Targets: +Creeper, Slime, Magma Cube, Guardian, Elder Guardian, Ravager
- Bane Damage: +2.5/lvl → +2.5/lvl (Slowness IV on hit, all targets)
- Thorns Damage: 1–5 → 1.35–6.75 (×1.35)
- Thorns Chance: 15%/lvl → 15%/lvl
- Thorns Hurt Cooldown: Starts/absorbed by attacker i-frames → Ignores i-frames
- Impaling vs Aquatic: +2.5/lvl → +2.5/lvl
- Impaling vs Wet Non-Aquatic (water, rain, bubble column): 0 → +1.5/lvl (60%)
- Enchanting Table Offer Pages: 1 → 3 (1–3 / 4–6 / 7+ lapis)
- Enchanting Lapis Costs: 1/2/3 → 1/2/3, 4/5/6, 7/8/9
- Enchanting XP Costs: Unchanged

## Villagers
- Diamond Gear Trades (Armorer, Toolsmith, Weaponsmith): Emeralds → Emeralds + ½ crafting diamonds (rounded up)
- Extra Diamonds: Helmet 3, Chestplate 4, Leggings 4, Boots 2, Pickaxe 2, Axe 2, Sword 1, Hoe 1, Shovel 1

## Transportation
- Minecart Top Speed (straight rails/slopes): 8 → 20 b/s
- Minecart Top Speed (curves): 8 → 8 b/s
- Minecart Top Speed on Rails in Water: 4 → 10 b/s
- Furnace Minecart Top Speed: 4 → 10 b/s
- Copper Rail Recipe: 6 Copper Ingots + 1 Stick → 8 Rails
- Rail Output (iron): 16 → 16
- Powered Rail Output: 6 → 12

## Potions / Alchemy
- Utility Potion Duration: ×1 → ×2 (Night Vision 3:00 → 6:00)
- Combat Potion Duration: ×1 → ×1.5 (Strength 3:00 → 4:30)
- Combat Effects: Strength, Regeneration, Resistance, Absorption, Health Boost
- Level II+ Potion Duration: ×1 → ×1
- Harmful Potion Duration: ×1 → ×1
- Drinkable Potion Stack Size: 1 → 4
- Splash/Lingering Potion Stack Size: 1 → 1
- Potion Mixing: None → Pour into water cauldron, bottle with empty bottle
- Mixed Potion Max Effects: 1 → 3
- Mixed Potion Output: All poured potions → 1 potion (−1 water level)
- Mixed Potion Kinds: Same kind only (drinkable/splash/lingering)
- Mixed Potion Rule: Each poured potion must add a new effect
- Mixed Potion Timed Duration: Arithmetic mean of original duration contributors
- Mixed Potion Instant Effects: Unchanged
- Mixed Potion Amplifiers: Unchanged (shared effect: higher amplifier wins)
- Resistance Potion: None → Resistance I, 3:00 (loot only, not brewable)

## Structures / Loot
- Mineshaft: +Minecart 20% or Lantern ×1–3 30%; +Night Vision 30%
- Nether Fortress: +Blaze Powder ×1–3 30% / Magma Cream ×1–2 20% / Glowstone Dust ×2–5 20%; +Fire Resistance 27% / Long Fire Resistance 7% / Strength 13%
- Bastion Treasure: +Long Fire Resistance 20% / Strength II 20% / Long Regeneration 10% / Resistance 10%
- Other Bastion Chests: +Fire Resistance 17% / Strength 8%
- Ruined Portal: +Fire Resistance 20%
- Shipwreck Supply: +Water Breathing 30% / Night Vision 20%; +Spyglass 10% / Compass 10%
- Shipwreck Treasure, Buried Treasure: +Long Water Breathing 20% / Luck 10%
- Ocean Ruins: Small +Water Breathing 20%; Big +Water Breathing 20% / Long Water Breathing 10% / Night Vision 10%
- Woodland Mansion: +Long Invisibility 14% / Strength 7% / Regeneration 7%
- Ancient City: +Long Night Vision 14% / Invisibility 7% / Resistance 7%; +Lapis ×4–12 30% / Bottle o' Enchanting ×2–5 20%
- End City Treasure: +Long Slow Falling 30% / Luck 10%
- Dungeon: +Swiftness 8% / Night Vision 8% / Regeneration 8%
- Jungle Temple: +Leaping 10% / Invisibility 10%
- Desert Pyramid: +Fire Resistance 10% / Swiftness 10%
- Stronghold Corridor/Crossing: +Slow Falling 10% / Night Vision 10%
- Witch Potion Drop (player kill): 0 → 8% + 2%/Looting
- Vanilla Loot Removed: None
- Resistance Potion Loot: Ancient City + Bastion Treasure
- Luck Potion Loot: Shipwreck Treasure + Buried Treasure + End City

## Luck / Exploration
- Luck Chest Bonus Rolls: None → +0.5 rolls per Luck level per pool
- Luck Rare Mob Drop Bonus Rolls (player-kill + random-chance pools): None → +0.5 rolls per Luck level
- Max Effective Luck: 4
- Negative Luck: No effect
- Luck Excluded: Fishing, Archaeology, Trial Chambers, Bonus Chest
