# Balance reference

Every gameplay rebalance that Progression Rebalance currently makes, grouped by system. For each one: what vanilla
does, what the mod changes, and the default numbers. Numbers are defaults; most are configurable in
`config/progressionrebalance.properties` (key shown in brackets). Implementation details are in
[docs/DESIGN.md](docs/DESIGN.md).

A shorter, player-facing version is [BALANCE_SUMMARY.md](BALANCE_SUMMARY.md).

**Keep this file (and BALANCE_SUMMARY.md) updated whenever a gameplay rebalance is added, removed or changed.**

The general rule of the mod: buff neglected vanilla mechanics rather than nerf strong ones, with two deliberate
exceptions: Protection (see [Enchantments](#enchantments)) and villager diamond gear, which now also costs
diamonds (see [Villagers](#villagers)). There are no new ore tiers, progression gates or new items; the only new
content is a loot-only Resistance potion and the potion cauldron block used for mixing.

---

## Equipment

**Gold is a fast, highly enchantable, high-performance but fragile equipment tier. Its armor protects better than
Iron but lacks Diamond's toughness and durability.** In vanilla, gold's only strengths (speed and enchantability)
never matter, because gold breaks almost at once and protects barely better than leather. The mod makes it a real
sidegrade between iron and diamond: better than iron where it counts in a fight, clearly worse to maintain, and
never "diamond but yellow". Every gold change can be switched off with `[gold.enabled]`.

### Golden tools (pickaxe, axe, shovel, hoe)
- **Vanilla:** 32 durability and wood-tier harvest level, so gold's top mining speed (12) is wasted.
- **Change:** 128 durability `[gold.toolDurability]`, just under stone (131) and far under iron (250). With
  `[gold.ironMiningTier]` golden tools harvest everything iron can (diamond ore yes; obsidian and ancient debris no).
- **Unchanged:** mining speed 12, enchantability 22, and the combat stats of the pickaxe, shovel and hoe.

### Golden weapons
| | Vanilla gold | **Gold (this mod)** | Iron | Diamond |
| --- | --- | --- | --- | --- |
| Sword attack damage | 4 | **6.5** `[gold.swordAttackDamage]` | 6 | 7 |
| Sword attack speed | 1.6 | **1.8** `[gold.swordAttackSpeed]` | 1.6 | 1.6 |
| Sword durability | 32 | **180** `[gold.swordDurability]` | 250 | 1561 |
| Axe attack damage | 7 | **8** `[gold.axeAttackDamage]` | 9 | 9 |
| Axe attack speed | 1.0 | **1.1** `[gold.axeAttackSpeed]` | 0.9 | 1.0 |
| Axe durability | 32 | **128** (tool durability) | 250 | 1561 |

- **Sword:** a full-strength hit recharges in 11 ticks instead of 12, so a golden sword lands about 9% more
  full-strength hits than iron or diamond in the same time. Each hit is between iron and diamond. Its durability
  is 72% of iron's: it is a real main weapon, but it needs repairing much more often than iron and wears out
  roughly 9 times faster than diamond.
- **Axe:** a little less damage per hit than iron and diamond, but the fastest of the three. It keeps the
  golden tool durability (128) because it is also a tool.
- **Enchantability:** 22, unchanged. It is the highest of all tool materials.
- **Gameplay purpose:** a gold weapon trades durability for tempo. It rewards players who fight often and keep
  enchanting and repairing, while diamond remains the low-maintenance, hardest-hitting choice.
- Only the vanilla golden sword and golden axe get the new combat stats. Durability follows the vanilla gold tool
  material.

### Golden armor
| | Vanilla gold | **Gold (this mod)** | Iron | Diamond |
| --- | --- | --- | --- | --- |
| Helmet / chestplate / leggings / boots | 2 / 5 / 3 / 1 | **3 / 7 / 5 / 3** `[gold.helmetArmor]` … `[gold.bootsArmor]` | 2 / 6 / 5 / 2 | 3 / 8 / 6 / 3 |
| Total armor | 11 | **18** | 15 | 20 |
| Armor toughness (full set) | 0 | **0** | 0 | 8 |
| Knockback resistance | 0 | **0** | 0 | 0 |
| Durability (helmet / chest / legs / boots) | 77 / 112 / 105 / 91 | **121 / 176 / 165 / 143** `[gold.armorDurabilityMultiplier = 11]` | 165 / 240 / 225 / 195 | 363 / 528 / 495 / 429 |
| Enchantability | 25 | **25** | 9 | 10 |

- **Durability relationship:** 73% of iron and 33% of diamond, per piece.
- **Why no toughness matters:** toughness reduces how much a big hit cuts through armor. Damage taken by a full
  set without enchantments:

  | Hit | Iron | **Gold** | Diamond | Vanilla gold |
  | --- | --- | --- | --- | --- |
  | 5 damage (an ordinary mob hit) | 2.5 | **1.9** | 1.25 | 3.3 |
  | 20 damage (a big explosion or heavy hit) | 16.0 | **13.6** | 8.0 | 18.2 |

  Against ordinary hits gold sits between iron and diamond. Against big hits it stays close to iron, while diamond
  takes little more than half of what gold takes.
- **Gameplay purpose:** better protection than iron plus the best enchantability, paid for with durability and
  no toughness. Iron stays the economical, long-lasting choice; diamond stays the best at everything but
  enchantability.
- Horse armor is unchanged (no durability; vanilla protection).

---

## Enchantments

Design rule: **general-purpose enchantments should be convenient and reliable. Specialized enchantments should be
substantially stronger when their specialization applies.** Vanilla Protection is so strong that the specialized
protections are never worth a slot, and several specialized enchantments apply too rarely to pick. The mod
narrows that gap from both sides, keeping every enchantment id.

### Protection
- **Vanilla:** each armor piece's enchantments add "protection points"; each point reduces damage by 4%, and the
  total is capped at 20 points (80%). Protection gives 1 point per level against almost all damage, so four
  pieces of Protection IV give 64%. Fire, Blast and Projectile Protection give 2 points per level against their
  damage only.
- **Change:** Protection gives 3% per level per piece `[enchantments.protectionReductionPerLevel = 3.0]`, i.e.
  0.75 points per level. Fire, Blast and Projectile Protection and the 80% cap are unchanged.
- **Resulting reductions** (four level-IV pieces, enchantments only; armor points apply before this):

  | Armor | General damage | Matching damage type |
  | --- | --- | --- |
  | 4× Protection | 48% (vanilla 64%) | 48% |
  | 3× Protection + 1 specialized | 36% | 68% |
  | 2× Protection + 2 specialized | 24% | 80% (cap) |

- **Intent:** full Protection stays the reliable all-rounder, but mixing in specialized pieces is now a real
  trade: less general protection, much more against the danger you are preparing for (the Nether, creepers,
  skeletons).

### Bane (Bane of Arthropods)
- **Vanilla:** +2.5 damage per level and Slowness IV on hit, against arthropods only (spiders, cave spiders,
  silverfish, endermites, bees). Sharpness is almost always better.
- **Change:** displayed as **"Bane"** (id still `minecraft:bane_of_arthropods`). It also affects creepers, slimes,
  magma cubes, guardians, elder guardians and ravagers. Damage bonus and Slowness are the vanilla values and apply
  to every target.
- **Targets** come from the entity tag `#progressionrebalance:bane_targets`, which is added to vanilla's
  `#minecraft:sensitive_to_bane_of_arthropods`. Data packs and mods can extend it; it is not a config option.
- **Intent:** Bane becomes the weapon for the mobs that punish close combat or have large health pools. Undead
  stay Smite's.

### Thorns
- **Vanilla:** 15% chance per level to reflect 1–5 damage to the attacker, costing 2 extra durability. The
  reflected hit also starts the attacker's damage cooldown ("invulnerability frames"), so the wearer's own
  counter-attack in the next half second is reduced or blocked. When the attacker is already in a cooldown, the
  reflection itself is reduced or blocked.
- **Change:** reflected damage ×1.35 `[enchantments.thornsDamageMultiplier]` → 1.35–6.75 (average about 4,
  vanilla 3). Thorns damage no longer starts, uses or resets the attacker's damage cooldown, in either direction.
- **Unchanged:** trigger chance, durability cost, and damage cooldowns for every other damage source (including
  guardian spikes, which use the same damage type but not the enchantment).
- **Intent:** Thorns becomes a small, dependable damage bonus that never works against you.

### Impaling
- **Vanilla:** +2.5 damage per level against aquatic mobs only.
- **Change:** targets that are not aquatic but are wet (in water, in rain, or in a bubble column; vanilla's own
  wetness check) take 60% of the bonus `[enchantments.impalingWetMultiplier = 0.6]`, i.e. +1.5 per level
  (+7.5 at Impaling V). Aquatic mobs still get the full +2.5 per level (+12.5 at V), never both bonuses.
  Dry non-aquatic targets get nothing.
- **Intent:** tridents with Impaling become a real choice in the rain and around water (drowned, rainy nights),
  while staying strongest in the ocean.

### Enchanting table pages
- **Vanilla:** the table shows three offers that cost 1, 2 and 3 lapis. There is no reason to put more than 3
  lapis in, and the only way to see different offers is to enchant something else.
- **Change:** the lapis in the table selects a *page* of three offers `[enchanting.lapisPages]`:
  - 1–3 lapis → page 1, costs 1/2/3 (exactly vanilla).
  - 4–6 lapis → page 2, costs 4/5/6.
  - 7+ lapis → page 3, costs 7/8/9 (`[enchanting.maxPages = 3]`).
- **Unchanged:** XP level costs, enchantment power, the screen layout. Each page's offers are fixed until you
  enchant (moving lapis in and out cannot reroll them), and enchanting changes all pages exactly as vanilla
  changes its offers.
- **Intent:** lapis becomes a real resource sink. Spending more lapis buys more choice instead of more power.

---

## Villagers

### Diamond gear trades
- **Vanilla:** armorers, toolsmiths and weaponsmiths sell enchanted diamond gear for emeralds only, so a trading
  hall skips the diamond age entirely.
- **Change:** those offers also ask for diamonds: half the item's crafting cost, rounded up, minimum 1
  `[villagers.diamondCostFraction = 0.5]`.

  | Item | Diamonds asked |
  | --- | --- |
  | Helmet | 3 |
  | Chestplate, leggings | 4 |
  | Boots, pickaxe, axe | 2 |
  | Sword, hoe, shovel | 1 |

- **Unchanged:** emerald prices, every non-diamond trade, and offers that villagers had already generated.
- **Intent:** villagers still save you half the diamonds plus the enchantment, but you have to have mined some
  diamonds. The trade is a shortcut through the diamond age, not a way around it. Switch off with
  `[villagers.enableTradeRebalance]`.

---

## Mining and resources

- **Gold as a mining tool:** golden pickaxes mine at iron tier (see [Golden tools](#golden-tools-pickaxe-axe-shovel-hoe)), so gold is
  useful the moment you find it.
- **Copper:** a new use as rail material (see [Rails](#rails)). Copper is plentiful and otherwise has few uses.
- **Lapis:** a new use as enchanting-page currency (see [Enchanting table pages](#enchanting-table-pages)).

---

## Transportation

### Minecarts
- **Vanilla:** top speed 8 blocks/s (0.4 blocks per tick) on rails. Rail lines are expensive to build and barely
  faster than sprint-jumping, so horses, elytra and ice boats make them obsolete.
- **Change:** 2.5× top speed on straight rails and slopes → 20 blocks/s `[transport.minecartSpeedMultiplier]`,
  range 1.0–2.5.
- **Curves** are taken at vanilla speed; the cart keeps its momentum and is back at full speed on the next
  straight block. Faster curves derail carts, so this is a safety limit, not a nerf.
- **Also ×2.5:** the on-rail cap in water (4 → 10 blocks/s) and the furnace minecart's cap (4 → 10 blocks/s).
- **Unchanged:** off-rail movement and the furnace minecart's push force.

### Rails
- **Vanilla:** rails need iron (6 iron + 1 stick → 16). Powered rails give 6 per craft (gold + redstone).
- **Change:**
  - New recipe: 6 copper ingots + 1 stick → **8 rails** `[rails.copperRailOutput]`.
  - Powered rails → **12** per craft `[rails.poweredRailOutput]`.
  - Iron rails stay at 16 `[rails.ironRailOutput]`.
- **Intent:** long rail lines become affordable before late-game iron farms, and powered rails (the gold sink of
  rail building) are less punishing.

---

## Potions and alchemy

### Potion durations
- **Vanilla:** most utility potions last 3:00 (8:00 extended), which is short for exploration.
- **Change:** each brewed potion falls into the first matching category:

  | Category | Rule | Multiplier | Example |
  | --- | --- | --- | --- |
  | Unchanged | No timed effect | – | Healing |
  | Amplified | Any effect at level II+ | ×1.0 `[potions.amplifiedDurationMultiplier]` | Strength II stays 1:30 |
  | Combat | Contains a combat effect | ×1.5 `[potions.combatDurationMultiplier]` | Strength 3:00 → 4:30 |
  | Harmful | All effects harmful | ×1.0 `[potions.harmfulDurationMultiplier]` | Poison stays 0:45 |
  | Utility | Everything else | ×2.0 `[potions.utilityDurationMultiplier]` | Night Vision 3:00 → 6:00 |

  Combat effects `[potions.combatEffects]`: strength, regeneration, resistance, absorption, health boost.
- Splash, lingering and tipped arrow durations follow from the potion (arrows keep vanilla's 1/8).
- **Intent:** exploration potions are worth brewing; fight-deciding potions only get a modest bump.

### Potion stacking
- **Vanilla:** potions do not stack.
- **Change:** drinkable potions stack to **4** `[potions.drinkableStackSize]`. Splash and lingering potions still
  do not stack (they are weapons).

### Potion mixing (potion cauldrons)
- **Vanilla:** one potion gives one effect, so a long expedition needs a hotbar full of bottles.
- **Change** `[potions.mixingEnabled]`:
  1. Use a potion on a water cauldron: it is poured in and the cauldron becomes a potion cauldron tinted with the
     potion's color.
  2. Pour in more potions of the same kind (drinkable, splash or lingering), up to **3 effects**
     `[potions.maxMixedEffects]`.
  3. Use an empty bottle: the whole mixture comes out as **one** "Mixed Potion" and one water level is used.
- **Rules:**
  - Each poured potion must add a new effect.
  - **Durations are averaged.** Every timed effect of the mixture lasts the average of the durations of *all*
    the potions poured in. Example: 16:00 + 12:00 + 3:00 gives three effects lasting 10:20 each.
  - The average always counts every original potion. Pouring a 3:00 potion into a 16:00 + 12:00 mixture gives
    10:20, not the 8:30 you would get by averaging the 14:00 mixture as one potion. Mixed potions remember what
    went into them, so this also holds when a bottled mixture is poured into another cauldron.
  - **Instant effects** (Instant Health, Instant Damage) stay instant. They take no part in the average and do
    not count as a 0:00 duration.
  - **Levels never change.** Strength II stays Strength II. When two potions share an effect, the higher level
    wins; levels and durations are never added together.
- **Intent:** mixing saves inventory space and gives a use for brewing many potion types, but it never creates
  more potion than you poured in (N potions in, 1 potion out). Averaging stops a mixture from getting the best
  duration of every ingredient, so one long potion cannot extend two short ones. It trades bottles for
  convenience, not for power.

### Resistance potion
- **Vanilla:** no Resistance potion exists.
- **Change:** a Potion of Resistance (Resistance I, 3:00) that is **only found in loot** (ancient cities and
  bastion treasure rooms). It cannot be brewed.

---

## Structures and loot

- **Vanilla:** most structure chests hold little that helps with the next step of the game, and brewing
  ingredients are rarely found where you need them.
- **Change:** thematic, additive pools. Nothing vanilla is removed and nothing is guaranteed.
  Chances are per chest, before Luck:

  | Structure | Additions (default chance) |
  | --- | --- |
  | Mineshaft | Minecart 20% or 1–3 lanterns 30%; Night Vision 30% |
  | Nether fortress | Blaze powder, magma cream or glowstone dust 70%; Fire Resistance / Strength potion 47% |
  | Bastion treasure room | Long Fire Resistance, Strength II, long Regeneration or Resistance 60% |
  | Other bastion chests | Fire Resistance or Strength 25% |
  | Ruined portal | Fire Resistance 20% |
  | Shipwreck supply | Water Breathing or Night Vision 50%; spyglass or compass 20% |
  | Shipwreck treasure, buried treasure | Long Water Breathing 20% or Luck 10% |
  | Ocean ruins | Small: Water Breathing 20%. Big: Water Breathing, Long Water Breathing or Night Vision 40% |
  | Woodland mansion | Long Invisibility, Strength or Regeneration 29% |
  | Ancient city | Long Night Vision, Invisibility or Resistance 29%; 4–12 lapis or 2–5 bottles o' enchanting 50% |
  | End city | Long Slow Falling 30% or Luck 10% |
  | Dungeon | Swiftness, Night Vision or Regeneration 25% |
  | Jungle temple, desert pyramid, stronghold | One utility potion 20% |
  | Witch (killed by a player) | One brewed potion (Fire Resistance, Water Breathing, Swiftness, Healing, Night Vision, Invisibility, Slow Falling or Regeneration), 8% + 2% per Looting level |

- Switch off with `[loot.enableStructureLootRebalance]`. Data packs that replace a loot table win.
- **Intent:** every structure becomes worth visiting for what its theme suggests (fire resistance where there is
  fire, water breathing near the ocean), without making any structure mandatory.

---

## Luck and exploration

### Luck
- **Vanilla:** Luck only affects fishing and a few loot entries, so the Potion of Luck is nearly worthless.
- **Change** `[luck.enabled]`:
  - Each point of Luck gives every structure chest pool a 50% chance of one extra roll
    `[luck.chestBonusRollChancePerLuck = 0.5]`. Luck I: 50% per pool; Luck II: one guaranteed extra roll per pool.
  - Rare player-kill mob drops (for example zombie iron, wither skeleton skulls) get the same bonus
    `[luck.mobDropBonusRollChancePerLuck = 0.5]`. Common and guaranteed drops are unchanged.
  - Capped at Luck 4 `[luck.maxEffectiveLuck]`. Negative Luck changes nothing and never removes loot.
- **Unchanged:** fishing (already uses Luck), archaeology, trial chamber rewards, the bonus chest.
- **Intent:** Luck becomes the "exploration" effect: drink it before raiding a structure for more of the same
  vanilla-style loot, not better tiers of loot. Potions of Luck are themselves rare loot (shipwreck and buried
  treasure, end cities).
