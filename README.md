# Progression Rebalance

A Vanilla+ progression rebalance for Minecraft **1.21.1** (Fabric). It makes existing vanilla mechanics worth
using instead of adding new tiers on top of them: gold gear becomes a real choice, specialized enchantments
become worth a slot, minecarts become a practical way to travel, and brewing, Luck and structure loot matter
again.

## Philosophy

- **Improve neglected vanilla mechanics** rather than add new content to replace them.
- **Make more progression choices viable**, so gold, rails, potions and specialized enchantments compete with
  the obvious picks.
- **Preserve vanilla's identity.** No new ore tiers, materials, progression ages, level gates or skill trees.
  Every item, id and screen stays recognisably vanilla.
- **Buff obsolete alternatives instead of nerfing strong mechanics.** There are only two reductions: slightly
  weaker Protection, so the specialized protections have room, and villager diamond gear that also costs some
  diamonds, so mining still matters.

## Features

- **Gold equipment:** a fast, highly enchantable but fragile tier. Golden tools last longer and mine at iron
  tier; the golden sword and axe hit harder and faster; golden armor protects better than iron but has no
  toughness and wears out faster.
- **Enchantments:** Protection is slightly weaker so that Fire, Blast and Projectile Protection are worth a
  slot. Bane of Arthropods is renamed **Bane** and also hits creepers, slimes, magma cubes, guardians and
  ravagers. Thorns deals more damage and no longer interferes with your own attacks. Impaling also works on
  wet targets (in water or rain).
- **Enchanting table pages:** extra lapis in the table shows more pages of offers. The first page is exactly
  vanilla, and pages cannot be rerolled by moving lapis in and out.
- **Villager trades:** armorers, toolsmiths and weaponsmiths still sell diamond gear, but also ask for some
  diamonds, so trading no longer skips the diamond age.
- **Minecarts and rails:** faster minecarts on straight track (curves stay safe), a copper rail recipe and more
  powered rails per craft.
- **Potions:** longer utility potions, modestly longer combat potions, drinkable potions stack to 4, and a
  loot-only Potion of Resistance.
- **Potion mixing:** pour up to three potions of the same kind into a water cauldron and bottle them as one
  Mixed Potion. Durations are averaged, and levels never change.
- **Structure loot:** thematic, additive chest loot (fire resistance in fortresses, water breathing in
  shipwrecks, supplies in mineshafts, and more). Witches can drop a brewed potion.
- **Luck:** the Luck effect adds bonus rolls to structure chests and rare mob drops.

Exact numbers: [BALANCE_SUMMARY.md](BALANCE_SUMMARY.md) (short list) and [BALANCE.md](BALANCE.md) (full reference
with vanilla comparisons).

## Requirements

| | |
| --- | --- |
| Minecraft | 1.21.1 (Java Edition) |
| Mod loader | Fabric Loader 0.19.5 or newer |
| Dependencies | [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.1 |
| Java | 21 (the version Minecraft 1.21.1 itself requires) |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1.
2. Put Fabric API and the Progression Rebalance jar in the `mods` folder.
3. For multiplayer, install both on the **server and on every client**.

The mod works in existing worlds. Villagers that already generated their trades keep them.

## Compatibility

- **Required on both client and server.** The mod registers a block (the potion cauldron) and a potion, and
  changes the enchanting table screen. A client without the mod cannot join a server that has it.
- **Data packs win.** Loot and enchantment changes apply only to the built-in definitions, so a data pack that
  replaces a loot table or an enchantment is used exactly as written. Rail output changes only apply while the
  vanilla recipe is unchanged.
- **Extendable.** Bane targets are the entity tag `#progressionrebalance:bane_targets`. Data packs can use the
  loot condition `progressionrebalance:is_wet` and the loot number provider `progressionrebalance:luck_bonus_rolls`.
- **Mixins** are small and each targets a single vanilla call site; none uses `@Overwrite`. Mods that replace the
  enchanting table entirely are expected to conflict. See [docs/DESIGN.md](docs/DESIGN.md#compatibility).

## Configuration

A commented file is written on first launch to `config/progressionrebalance.properties`. Every option lists its
default and valid range.

- Sections: transport, rails, gold, enchanting, enchantments, potions, luck, villagers and loot. Most systems
  have an on/off switch, and their numbers can be tuned.
- Changes take effect after a **restart**.
- Invalid values are logged and replaced with the default; missing keys are added back automatically.
- In multiplayer, **use the same file on the server and every client.** Durability, weapon and armor stats,
  stack sizes and potion durations are computed on each side, so different values cause wrong tooltips.

## Documentation

| File | Contents |
| --- | --- |
| [BALANCE_SUMMARY.md](BALANCE_SUMMARY.md) | Every balance change as a short `old → new` list |
| [BALANCE.md](BALANCE.md) | Every rebalance with vanilla behavior, default values, config keys and intent |
| [docs/DESIGN.md](docs/DESIGN.md) | Design rationale, implementation notes and compatibility details |
| [MANUAL_TESTING.md](MANUAL_TESTING.md) | In-game checklist for what automated tests cannot cover |
| [CHANGELOG.md](CHANGELOG.md) | Release history |

## Building from source

Requires JDK 21.

```bash
./gradlew build
```

Compiles the mod and runs the unit tests. The mod jar is written to `build/libs/`.

```bash
./gradlew runGametest
```

Starts a headless dedicated server and runs the in-world GameTests in `src/gametest` (report in
`build/gametest/junit.xml`). The GameTests are never included in the mod jar.

```bash
./gradlew runDatagen
```

Regenerates the data in `src/main/generated` (recipe, tags, block state and translations).

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Known limitations

- Villagers keep trades generated before the mod was installed.
- Golden weapons are used by mobs too: piglins, zombified piglins and piglin brutes hit harder.
- Mobs still judge golden armor by its vanilla protection when deciding whether to pick it up.
- Minecarts slow to vanilla speed on curves; faster curves would derail them.
- Two mixed potions with the same effects but different ingredients do not stack with each other.
- Translations: English and Spanish (Spain) only.

## License

All Rights Reserved. See [LICENSE.txt](LICENSE.txt).
