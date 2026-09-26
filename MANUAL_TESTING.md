# Manual testing

Automated coverage: `./gradlew build` (unit tests) and `./gradlew runGametest` (27 in-world tests). The checks
below cover what automation cannot: rendering, client sync, feel and multiplayer.

Start with `./gradlew runClient` in a new **Survival** world with cheats enabled, and default config unless stated.

## Release jar smoke test
The dev environment runs with named mappings; the release jar is remapped. Before publishing, test the built jar
from `build/libs/` (not the `-sources` jar) outside the dev environment:
- [ ] A normal Fabric 1.21.1 client (launcher profile) with only Fabric API and the jar starts and opens a new world without errors.
- [ ] A dedicated Fabric 1.21.1 server with the same two mods starts; a client with the mod joins it.
- [ ] A client **without** the mod is refused by that server with a clear message, not a crash.
- [ ] Spot checks in that world: golden sword tooltip 6.5 / 1.8, enchanting page 2 with 4+ lapis, a minecart on straight powered rail is fast, a potion pours into a cauldron.

## 0. Startup
- [ ] `config/progressionrebalance.properties` is created, with comments.
- [ ] The log has no `progressionrebalance` warnings.
- [ ] Set `gold.toolDurability=abc`, restart. A warning is logged and the default (128) is used.

## 1. Gold equipment
Turn on advanced tooltips (F3+H) and `/gamerule naturalRegeneration false`. Read health with
`/data get entity @e[type=<mob>,limit=1,sort=nearest] Health` and heal with `/effect give @s instant_health 1 5`.
Hits must be fully charged and **not** critical (don't jump).

### Gold sword
- [ ] **Tooltips:** golden sword shows 6.5 Attack Damage / 1.8 Attack Speed; iron sword 6 / 1.6; diamond sword 7 / 1.6.
- [ ] **Gold vs Iron damage:** summon `/summon iron_golem ~ ~ ~3 {NoAI:1b}` (100 health, no armor). One full hit takes 6.5 with gold and 6 with iron.
- [ ] **Gold vs Diamond damage:** one full hit with a diamond sword takes 7, so diamond still hits harder.
- [ ] **Cooldown, visually:** with the attack indicator on (Options → Video → Attack Indicator: Crosshair), the golden sword's indicator refills noticeably faster than an iron or diamond sword's.
- [ ] **Gold attacks noticeably faster:** hit a NoAI golem for 10 s at full charge only. Gold lands about 18 full hits; iron and diamond about 16.
- [ ] **Durability usable but limited:** the golden sword shows 180 durability. It kills roughly 45 zombies before breaking (4 hits each), against roughly 60 for iron and several hundred for diamond. It feels like a real weapon that needs repairing often.

### Gold armor
- [ ] **Full gold armor points:** wearing the full set shows 9 armor icons; `/attribute @s minecraft:generic.armor get` returns 18.
- [ ] **Compare against full Iron:** 15 (7.5 icons).
- [ ] **Compare against full Diamond:** 20 (10 icons).
- [ ] **No toughness:** `/attribute @s minecraft:generic.armor_toughness get` returns 0 in gold, 8 in diamond. `minecraft:generic.knockback_resistance` is 0.
- [ ] **Ordinary mob damage:** `/damage @s 5 minecraft:mob_attack` (no enchantments, no effects) takes 2.5 in iron, **1.9** in gold, 1.25 in diamond (`/data get entity @s Health`).
- [ ] **High-damage hit:** `/damage @s 20 minecraft:mob_attack` takes 16 in iron, **13.6** in gold, 8 in diamond.
- [ ] **Diamond's toughness advantage is noticeable:** a creeper explosion at point-blank range, or a ravager's hit, hurts much more in gold than in diamond, while ordinary zombie and skeleton hits feel close.
- [ ] **Gold durability meaningfully worse:** golden chestplate 176 durability, iron 240, diamond 528. In normal play, gold armor needs repairing clearly more often than iron.
- [ ] **Horse armor:** golden horse armor still has no durability bar.

### Regression
- [ ] **Gold pickaxe/tool behaviour:** a golden pickaxe mines diamond ore and drops a diamond; obsidian drops nothing. Shovels, hoes and axes still work as tools.
- [ ] **Mining speed unchanged:** a golden pickaxe still mines stone faster than a diamond pickaxe (no Efficiency on either).
- [ ] **Tool durability did not regress:** golden pickaxe, axe, shovel and hoe show 128 durability.
- [ ] **Pickaxe combat unchanged:** golden pickaxe tooltip is still 2 Attack Damage / 1.2 Attack Speed.
- [ ] **Enchantability stays high:** at a table with 15 bookshelves, golden swords and armor regularly get higher-level or multiple enchantments compared with diamond gear.
- [ ] **Nether mobs:** zombified piglins and piglins with golden swords hit harder than before (about 10.5 instead of 8 before difficulty scaling). Check that the Nether still feels fair on Normal and Hard.

## 2. Villagers
- [ ] Spawn a **new** armorer, toolsmith and weaponsmith.
- [ ] Level them up to master: `/data merge entity @e[type=villager,limit=1,sort=nearest] {VillagerData:{level:5}}`, then trade once to refresh.
- [ ] Every diamond gear offer asks for emeralds **plus** diamonds, e.g. chestplate 4, pickaxe 2, sword 1.
- [ ] Buying consumes both, and non-diamond trades are unchanged.

## 3. Enchanting pages
Setup: a table with 15 bookshelves, 30+ levels, and a diamond sword in the item slot.

- [ ] **1–3 lapis:** vanilla offers, costs 1/2/3.
- [ ] **4–6 lapis:** different offers.
  - [ ] The lapis icons and tooltip show 4/5/6.
  - [ ] The tooltip has an "Offer page 2" line.
- [ ] **7+ lapis:** page 3, costs 7/8/9. A full stack stays on page 3.
- [ ] **Repeated switching:** moving between 2 and 5 lapis several times does not change either page's offers.
- [ ] **Enchant on page 2:** exactly the page cost in lapis is used and the vanilla XP level cost is charged.
- [ ] **After enchanting:** a new item shows new offers on every page (the seed changed).
- [ ] **Too little lapis:** with 5 lapis, the third page-2 offer (6 lapis) is greyed out and cannot be clicked.
- [ ] **Reconnect:** close and reopen the table, and rejoin the world. The same offers are shown for the same lapis count.

## 4. Enchantments
Setup for every check:
- `/gamerule naturalRegeneration false`, and heal between measurements with `/effect give @s instant_health 1 5`.
- Read a mob's health with `/data get entity @e[type=<mob>,limit=1,sort=nearest] Health`, and your own with
  `/data get entity @s Health`.
- Spawn test mobs with `NoAI:1b` so they stand still, e.g. `/summon creeper ~ ~ ~3 {NoAI:1b}`.
- Give enchanted items with the 1.21.1 component syntax, e.g.
  `/give @s diamond_chestplate[enchantments={levels:{"minecraft:protection":4}}]`.
- Melee hits must be fully charged and **not** critical (don't jump). Crits multiply only the base damage.

### Protection
Wear four pieces of the same material and have no active effects (`/effect clear @s`). `/damage @s 10 minecraft:generic` ignores armor points, so it shows the
enchantment reduction alone. Fire (`minecraft:on_fire`) also ignores armor points. For blast
(`minecraft:explosion`) and projectile (`minecraft:arrow`) damage, armor points apply first, so compare with the
same armor unenchanted: the enchanted damage should be the unenchanted damage × the factor below.

| Armor (all level IV) | `generic` (health lost) | Matching specialized damage |
| --- | --- | --- |
| No enchantments | 10 | 10 |
| Full Protection IV | **5.2** (vanilla 3.6) | 5.2 (×0.52) |
| 2× Protection + 2× Fire Protection | 7.6 | `on_fire`: **2.0** (cap) |
| 2× Protection + 2× Blast Protection | 7.6 | `explosion`: ×0.20 (cap) |
| 2× Protection + 2× Projectile Protection | 7.6 | `arrow`: ×0.20 (cap) |
| 4× Fire Protection | 10 | `on_fire`: **2.0**, not less |

- [ ] **Full Protection IV:** `generic` takes 5.2 (48% reduction).
- [ ] **2× Protection IV + 2× Fire Protection IV:** 7.6 from `generic`, 2.0 from `on_fire`.
- [ ] **2× Protection IV + 2× Blast Protection IV:** 7.6 from `generic`; `explosion` damage is 20% of the unenchanted value.
- [ ] **2× Protection IV + 2× Projectile Protection IV:** 7.6 from `generic`; `arrow` damage is 20% of the unenchanted value.
- [ ] **General protection is weaker:** full Protection IV loses 5.2, not vanilla's 3.6.
- [ ] **Specialization is substantially stronger:** against its own damage, a 2+2 set takes 2.0 where full Protection IV takes 5.2.
- [ ] **The cap still works:** 4× Fire Protection IV still takes 2.0 from `on_fire` (80%, never 100%).
- [ ] **Survival feel:** real fire, creeper explosions and skeleton arrows hurt less with the matching specialized pieces than with full Protection.

### Bane
Use two wooden swords: one plain and one `wooden_sword[enchantments={levels:{"minecraft:bane_of_arthropods":1}}]`.
For each mob, hit a fresh NoAI mob with each sword and compare the health lost. The plain sword deals 4; Bane I
should deal **2.5 more** (6.5) and give Slowness IV (visible particles, or `active_effects` in `/data get`).
Magma cubes have armor points, so their numbers are a little lower, but the Bane sword must still deal more.

- [ ] Spider (`/summon spider ~ ~ ~3 {NoAI:1b}`)
- [ ] Cave spider
- [ ] Silverfish
- [ ] Creeper
- [ ] Slime (`{NoAI:1b,Size:3}` so it survives the hit)
- [ ] Magma cube (`{NoAI:1b,Size:3}`)
- [ ] Guardian (on land with NoAI, or in water)
- [ ] Ravager
- [ ] Optional: endermite and elder guardian (also in the tag), and a bee (vanilla arthropod, still a target).
- [ ] **Negative control, normal zombie** (`{NoAI:1b,IsBaby:0b}`): both swords deal the same damage and there is no Slowness.
- [ ] **Display name:** the sword's tooltip and the enchanting table hover read **"Bane"** (and "Perdición" in Español (España)); `/give … "minecraft:bane_of_arthropods"` still works.
- [ ] **Data pack:** a data pack that adds `minecraft:zombie` to `data/progressionrebalance/tags/entity_type/bane_targets.json` makes the Bane sword deal the bonus to zombies after `/reload`.

### Thorns
Wear `diamond_chestplate[enchantments={levels:{"minecraft:thorns":3}}]` (45% chance per hit) and give yourself
`/effect give @s resistance 600 3` so you survive. Use an attacker without armor points, for example a spider
or a vindicator (`/summon vindicator`), and read its health after each hit it lands.

- [ ] **Receive melee attacks while wearing Thorns.** When Thorns triggers, the attacker flashes red and the chestplate loses 2 extra durability, as in vanilla.
- [ ] **Reflected damage is ~35% stronger:** each reflection removes between 1.35 and 6.75 health (vanilla 1–5). Over about 10 reflections, some should exceed 5 (impossible in vanilla), and the average should be about 4 (vanilla 3).
- [ ] **Immediately attack after Thorns triggers:** charge a sword *before* the attacker hits you, then hit it straight away (within half a second) after a Thorns reflection.
- [ ] **Sword damage is NOT blocked by Thorns i-frames:** that hit deals the sword's **full** damage (e.g. 7 with a diamond sword), on top of the Thorns damage. In vanilla it would be reduced or blocked.
- [ ] **Normal i-frames still exist elsewhere:**
  - [ ] Hitting a mob twice within half a second (two players, or a sword hit and then an arrow) still has the second hit blocked or reduced as in vanilla.
  - [ ] Guardian spikes (which also deal thorns-type damage, but not from the enchantment) behave as in vanilla.
  - [ ] Mobs in lava, fire or cactus take damage at the vanilla rate, not every tick.
- [ ] **No loops:** two players (or a player and a mob given Thorns armor) hitting each other while both wear Thorns reflect only once per hit.

### Impaling
Use `trident[enchantments={levels:{"minecraft:impaling":5}}]` in melee (9 base damage). Impaling V is +12.5 against
aquatic mobs and +7.5 (60%) against other wet mobs. Zombies have 2 armor points, so they lose slightly less than
the raw numbers. Summon zombies with `{NoAI:1b,IsBaby:0b}` at night (or give them a helmet) so they don't burn.

- [ ] **Aquatic mob** (guardian or squid): the full bonus, 21.5 damage (a guardian goes from 30 to 8.5).
- [ ] **Dry zombie:** no bonus, about 8.9 lost.
- [ ] **Zombie standing in water** (feet in a 1-deep pool): the 60% bonus, about 16.2 lost.
- [ ] **Zombie exposed to rain** (`/weather rain`, open sky, a biome where it rains rather than snows): about 16.2 lost.
- [ ] **Zombie under cover during rain** (one block above its head, dry ground): no bonus, about 8.9 lost.
- [ ] **Full bonus vs aquatic:** a guardian standing in water still loses 21.5, not 21.5 + 7.5.
- [ ] **60% bonus vs wet normal entity:** the wet zombie loses about 7.4 more than the dry one (7.5 minus armor).
- [ ] **Zero bonus vs dry normal entity:** the dry zombie loses the same as with an unenchanted trident.
- [ ] **Thrown trident:** a thrown Impaling trident follows the same rules (8 base damage).

## 5. Minecarts
- [ ] **Straight:** on a 100-block powered track, the cart reaches 20 blocks/s (time it: 100 blocks ≈ 5 s).
- [ ] **Curves:** a cart takes curves at full speed without derailing or clipping through blocks. It briefly slows to vanilla speed in the curve block and is back at full speed right after.
- [ ] **Diagonals:** a staircase of alternating curves works too, at vanilla speed.
- [ ] **Slopes:** a cart climbs a 3-block powered slope and descends one without leaving the track.
- [ ] **Passengers:** with a player or mob passenger, a cart still stays on the rails, including during chunk loading on a long track.
- [ ] **Multiplayer:** on a dedicated server (`./gradlew runServer` plus a client), riding looks smooth, with no rubber-banding.
- [ ] **Other carts:** chest and hopper carts behave normally, and a furnace cart still pushes.

## 6. Rails
- [ ] Crafting shows 8 rails from 6 copper ingots + stick, 16 from iron, and 12 powered rails.
- [ ] The recipe book unlocks the copper recipe after picking up copper.

## 7. Potions
- [ ] **Durations:**
  - [ ] Night Vision (3:00 in vanilla) shows **6:00**.
  - [ ] Strength shows **4:30**.
  - [ ] Strength II stays **1:30**.
  - [ ] Poison stays **0:45**.
- [ ] **Stacking:** drinkable potions stack to 4; splash and lingering potions do not stack. Drinking one from a stack leaves 3 and gives a bottle.
- [ ] **Pouring:** use Night Vision on a full water cauldron.
  - [ ] The bottle comes back empty.
  - [ ] The liquid turns Night Vision's color, with faint particles.
  - [ ] The water level stays at 3.
- [ ] **Second potion:** pour in Fire Resistance. The color blends.
- [ ] **Bottling:** use an empty bottle.
  - [ ] You get one **"Mixed Potion"** (not "Uncraftable Potion") with both effects in the tooltip.
  - [ ] The cauldron is plain water with 2 levels.
- [ ] **Three effects:** pour in Night Vision, Fire Resistance and Water Breathing, then bottle. The potion has 3 effects.
- [ ] **Too many:** pouring a 4th effect is rejected with a red message. The potion stays in your hand and is not drunk.
- [ ] **Duplicates:**
  - [ ] Pouring Swiftness II into a cauldron holding Swiftness is rejected ("no new effect").
  - [ ] Pour Swiftness II (1:30) and Night Vision (6:00), then bottle the mix (both 3:45). Pour plain Swiftness (6:00) into a fresh cauldron, then pour in that mix. Bottling gives Swiftness **II** plus Night Vision, both **4:30** ((6:00 + 1:30 + 6:00) / 3), never added together.
- [ ] **Kinds:**
  - [ ] A splash potion poured into a drinkable mixture is rejected.
  - [ ] A splash-only mixture bottles as a "Mixed Splash Potion", and lingering as a "Mixed Lingering Potion".
- [ ] **Single potion:** pour one potion and bottle it straight away. You get the same potion back, with its normal name.
- [ ] **Vanilla behaviour:**
  - [ ] Water bottles still fill a water cauldron.
  - [ ] Sneaking with a potion drinks it instead of pouring.
  - [ ] Awkward potions are not poured.
- [ ] **Breaking:** breaking a potion cauldron with a pickaxe drops a cauldron.
- [ ] **Persistence:** leave and rejoin the world. The mixture and its color are still there.
- [ ] **Splash:** a mixed splash potion applies all of its effects when thrown.
- [ ] **Drinking:** a mixed drinkable potion applies all of its effects.
- [ ] **Tipped arrows:** 8 arrows + a mixed lingering potion craft into "Mixed Tipped Arrow"s.
- [ ] **Language:** switch the game to Español (España). The names read "Poción mezclada" and "Caldero con poción".
- [ ] **Multiplayer:** on a dedicated server, pouring and bottling work with no ghost items, and other players see the tint.

### Duration averaging
Brewed durations with this mod: Long Night Vision 16:00, Long Strength 12:00, Long Fire Resistance 16:00,
Swiftness 6:00, Night Vision 6:00, Slow Falling 3:00, Strength II 1:30. Read durations from the potion tooltip.

- [ ] **2 potions:** pour Long Night Vision (16:00) and Long Strength (12:00), then bottle. Both effects show **14:00**.
- [ ] **3 potions:** pour Long Night Vision (16:00), Long Strength (12:00) and Slow Falling (3:00), then bottle. All three show **10:20**.
- [ ] **Already-mixed potion:** bottle the 2-potion mixture above (14:00). In a fresh water cauldron pour Slow Falling (3:00), then pour in the 14:00 mixture, then bottle. All three show **10:20**, not 8:30.
  - [ ] The same result when the order is reversed: mixture first, then Slow Falling.
- [ ] **Instant effects excluded:** pour Long Night Vision (16:00), Swiftness (6:00) and Healing II, then bottle. Night Vision and Speed show **11:00**; Instant Health II has no duration in the tooltip, and drinking it heals instantly (4 hearts).
- [ ] **Amplifiers preserved:** pour Strength II (1:30) and Night Vision (6:00), then bottle. The tooltip shows **Strength II** and Night Vision (level I), both 3:45. Drinking it gives Strength II.
- [ ] **Save/load persistence:**
  - [ ] Put a bottled 2-potion mixture (14:00) in a chest, quit to title, restart the game (or restart the server), and reload. Take it out and pour it with Slow Falling as above: the result is still **10:20**.
  - [ ] Pour Long Night Vision and Long Strength into a cauldron **without** bottling, restart, then pour in Slow Falling and bottle: **10:20**.
  - [ ] Moving the mixed potion between inventory slots, a shulker box and an ender chest does not change the result.

## 8. Luck
- [ ] Use `/effect give @s luck 600 1` (Luck II) and open 10+ fresh mineshaft chests (`/locate structure mineshaft`, or `/loot spawn ~ ~ ~ loot minecraft:chests/abandoned_mineshaft`).
- [ ] Compare them with the same number opened without luck. Lucky chests should hold noticeably more stacks, but still vanilla-style items.
- [ ] Kill zombies with Luck II. Iron, carrots and potatoes drop more often than without it, and rotten flesh is unchanged.

## 9. Structures
Use `/loot spawn ~ ~ ~ loot <table>` repeatedly, or explore:
- [ ] `minecraft:chests/nether_bridge` sometimes has fire resistance, blaze powder or magma cream.
- [ ] `minecraft:chests/shipwreck_supply` sometimes has water breathing or night vision.
- [ ] `minecraft:chests/ancient_city` occasionally has a "Potion of Resistance".
- [ ] `minecraft:chests/end_city_treasure` occasionally has a Potion of Luck.
- [ ] Vanilla items still appear at their usual rates.
