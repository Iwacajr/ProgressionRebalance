# Modrinth release sheet

Values to enter when creating the Modrinth project and its first version by hand. Nothing here is published
automatically.

## Project

| Field | Value |
| --- | --- |
| Project name | Progression Rebalance |
| Suggested slug | `progression-rebalance` |
| Summary | A Vanilla+ rebalance that makes gold gear, specialized enchantments, minecarts, potions and Luck worth using, without new tiers or progression gates. |
| Project type | Mod |
| Game | Minecraft: Java Edition |
| Client-side | Required |
| Server-side | Required |
| License | All Rights Reserved (`LicenseRef-All-Rights-Reserved`), from `LICENSE.txt` and `fabric.mod.json` |
| Description | Contents of [`MODRINTH_DESCRIPTION.md`](../MODRINTH_DESCRIPTION.md) (replace `OWNER/REPOSITORY` in its links) |

**Environment.** The mod must be installed on both sides: it registers a block and a potion (Fabric registry
sync refuses clients without them), adds a synced property to the enchanting table screen, and has client code
for the enchanting screen and the potion cauldron tint. In single player it only needs to be in the game's
`mods` folder.

### Suggested categories

- Primary: **Game Mechanics**, **Equipment**, **Transportation**
- Additional: **Adventure** (structure loot and Luck)

### Links to fill in

- Source code / issues: the GitHub repository, once created.
- Icon and gallery images: none exist in the repository yet.

## Version

| Field | Value |
| --- | --- |
| Version number | `1.0.0-beta.1` (matches `mod_version` in `gradle.properties`) |
| Version title | Progression Rebalance 1.0.0-beta.1 for Minecraft 1.21.1 |
| Release channel | Beta |
| Minecraft versions | 1.21.1 |
| Loaders | Fabric |
| File to upload | `build/libs/progression-rebalance-1.0.0-beta.1.jar` |
| Do not upload | `build/libs/progression-rebalance-1.0.0-beta.1-sources.jar` (source archive, not a mod) |

### Dependencies

| Project | Type |
| --- | --- |
| Fabric API | Required |

There are no optional dependencies. Fabric Loader 0.19.5 or newer is required by `fabric.mod.json`, but the
loader is selected by the player, not listed as a Modrinth dependency.

### Changelog

```markdown
First public release, for Minecraft 1.21.1 (Fabric).

**Gold equipment**
- Golden tools: 128 durability (vanilla 32) and iron harvest tier.
- Golden sword: 6.5 damage, 1.8 attack speed, 180 durability. Golden axe: 8 damage, 1.1 attack speed.
- Golden armor: 18 armor points in total (vanilla 11), no toughness, durability between vanilla gold and iron.

**Enchantments**
- Protection 3% per level instead of 4%, so Fire, Blast and Projectile Protection are worth a slot.
- Bane of Arthropods is now "Bane" and also hits creepers, slimes, magma cubes, guardians and ravagers.
- Thorns deals 35% more damage and no longer blocks your counter-attacks.
- Impaling deals 60% of its bonus to wet mobs.
- Enchanting table pages: more lapis shows more offers (1–3 / 4–6 / 7+ lapis).

**Villagers**
- Diamond gear trades also cost half the item's crafting diamonds, rounded up.

**Transportation**
- Minecarts reach 20 blocks/s on straight track; curves stay at vanilla speed.
- Copper rail recipe (6 copper + stick → 8 rails); powered rails 6 → 12 per craft.

**Potions**
- Utility potions last twice as long, combat potions 50% longer; level II potions unchanged.
- Drinkable potions stack to 4.
- Mix up to 3 potions in a water cauldron; durations are averaged.
- New loot-only Potion of Resistance.

**Loot and Luck**
- Thematic additions to structure chests; nothing vanilla removed.
- Witches can drop a brewed potion.
- Luck adds bonus rolls to structure chests and rare mob drops.

Most values are configurable, and each system can be switched off, in `config/progressionrebalance.properties`. Use the same config on server and clients.
```
