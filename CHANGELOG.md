# Changelog

All notable changes to this project are documented here. Balance numbers are defaults; see
[BALANCE_SUMMARY.md](BALANCE_SUMMARY.md) for the full list.

## [Unreleased]

## [1.0.0-beta.1] - Minecraft 1.21.1

First public release.

### Gold equipment
- Golden tools: 128 durability (vanilla 32) and iron harvest tier. Mining speed and enchantability unchanged.
- Golden sword: 6.5 attack damage, 1.8 attack speed, 180 durability (vanilla 4 / 1.6 / 32).
- Golden axe: 8 attack damage, 1.1 attack speed (vanilla 7 / 1.0).
- Golden armor: 3/7/5/3 armor points (18 total, vanilla 11), no toughness or knockback resistance, durability
  multiplier 11 (vanilla 7).

### Enchantments
- Protection: 3% damage reduction per level per piece instead of 4% (full Protection IV: 48%, vanilla 64%). Fire,
  Blast and Projectile Protection and the 80% cap are unchanged.
- Bane of Arthropods is displayed as "Bane" (id unchanged) and also affects creepers, slimes, magma cubes,
  guardians, elder guardians and ravagers, through the entity tag `#progressionrebalance:bane_targets`.
- Thorns: reflected damage ×1.35. Thorns damage no longer starts or is absorbed by the attacker's damage
  cooldown.
- Impaling: 60% of its bonus against wet non-aquatic targets (in water, rain or a bubble column).
- Enchanting table pages: 1–3 lapis shows the vanilla offers (costs 1/2/3), 4–6 lapis a second page (4/5/6),
  7+ lapis a third page (7/8/9). XP costs unchanged.

### Villagers
- Armorer, toolsmith and weaponsmith offers for diamond gear also cost half the item's crafting diamonds,
  rounded up.

### Transportation
- Minecart top speed ×2.5 on straight rails and slopes (8 → 20 blocks/s). Curves keep vanilla speed.
- New copper rail recipe: 6 copper ingots + 1 stick → 8 rails.
- Powered rail recipe output 6 → 12.

### Potions
- Brewed potion durations: utility ×2, combat ×1.5; level II and harmful potions unchanged.
- Drinkable potions stack to 4.
- Potion mixing in water cauldrons: up to 3 effects of the same potion kind, bottled as one Mixed Potion.
  Timed effects last the average duration of every potion poured in; instant effects and levels are unchanged.
- Potion of Resistance (Resistance I, 3:00), found only in ancient city and bastion treasure loot.

### Loot and Luck
- Additive, thematic loot in 20 vanilla structure chest loot tables; nothing vanilla is removed.
- Witches killed by a player can drop a brewed potion (8% + 2% per Looting level).
- Luck adds bonus rolls to structure chest pools and rare player-kill mob drops (+0.5 per Luck level, up to
  Luck 4).

### Technical
- Config file `config/progressionrebalance.properties`, documented and range-checked.
- Data pack hooks: loot condition `progressionrebalance:is_wet`, loot number provider
  `progressionrebalance:luck_bonus_rolls`, damage type tag `#progressionrebalance:ignores_hurt_cooldown`.
- Translations: English and Spanish (Spain).
