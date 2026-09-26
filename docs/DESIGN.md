# Design notes

## Philosophy

Progression Rebalance **buffs neglected vanilla mechanics instead of nerfing strong ones**. Players should still be
able to play exactly as in vanilla. The mod only gives them more reasons to use gold, minecarts, brewing, Luck and
structure exploration. It adds no progression ages, level gates, new ore tiers or new items.

When choosing how to implement something, the order of preference was:

1. Vanilla data (recipes, loot, lang), generated with Fabric datagen.
2. Fabric API events (`LootTableEvents`, `DefaultItemComponentEvents`, `UseBlockCallback`).
3. Accessors.
4. Small mixins, each documented with why an event was not enough.

Every balance number lives in `ModConfig` (the single source of truth) and is exposed in
`config/progressionrebalance.properties`. The player-facing list of every rebalance is
[BALANCE.md](../BALANCE.md); keep it in sync with this file.

## Systems

### Gold equipment (`equipment/GoldEquipmentRebalance`)

Gold is a fast, highly enchantable, high-performance but fragile equipment tier. Its armor protects better than
iron but lacks diamond's toughness and durability. Vanilla gold has the right strengths (speed 12, enchantability
22 for tools and 25 for armor) but breaks almost immediately and protects barely better than leather, so players
never use it. The intended order is iron → gold → diamond, but each stat is placed for the identity, not simply
between the two:

| Stat | Gold vs iron | Gold vs diamond |
| --- | --- | --- |
| Sword damage per hit | higher (6.5 vs 6) | lower (7) |
| Sword attack speed | faster (1.8 vs 1.6) | faster (1.6) |
| Armor points | higher (18 vs 15) | lower (20) |
| Armor toughness | same (0) | much lower (8) |
| Durability | lower (72–73%) | far lower (about 1/3 for armor, 1/9 for the sword) |
| Enchantability | far higher | far higher |

- **Tools.** Durability goes from 32 to 128 (`gold.toolDurability`), just under stone (131).
- **Harvest tier.** With `gold.ironMiningTier`, gold tools use the iron "incorrect for" tag. A golden
  pickaxe can then mine diamond ore but not obsidian or ancient debris.
- **Sword.** 6.5 attack damage and 1.8 attack speed (vanilla 4 / 1.6): a full-strength hit every 11 ticks
  instead of 12 (vanilla counts a hit as charged half a tick early). That is a visible but modest advantage,
  not spam clicking. Durability 180, 72% of iron
  (`gold.swordDurability`). Swords get their own durability because a weapon loses durability per hit, and the
  tool value (128) would make gold a poor main weapon. Swords are detected as gold-material `SwordItem`s, so
  the pickaxe, shovel, hoe and axe keep the tool value.
- **Axe.** 8 attack damage and 1.1 attack speed (vanilla 7 / 1.0; iron 9 / 0.9; diamond 9 / 1.0). It keeps the
  tool durability (128): it is first a tool, and the existing tool balance is kept.
- **Armor.** Armor points become 3/7/5/3 = 18 (vanilla 2/5/3/1 = 11). Every piece is at least iron's and at most
  diamond's. The chestplate, the piece with the most room between iron (6) and diamond (8), is where gold sits in
  between. Toughness stays 0 and there is no knockback resistance, so diamond still takes large hits far better
  (see the table in BALANCE.md). The durability multiplier goes from 7 to 11: 121/176/165/143, which is 73% of
  iron's 165/240/225/195.
- **Horse armor** has no durability and is skipped entirely.

How it works:

- Only default item components are modified, through `DefaultItemComponentEvents`. Existing stacks pick up the
  new values automatically, and components set on a stack by other mods or commands still win.
- **Attack damage and speed** rewrite the vanilla `minecraft:base_attack_damage` / `minecraft:base_attack_speed`
  modifiers in the `attribute_modifiers` component of `golden_sword` and `golden_axe` only. The config values are
  the totals a player sees (the player's base 1 damage and 4 speed are subtracted). Modded weapons built on the
  gold tool material keep their own combat stats.
- **Armor points.** Vanilla armor items have an empty `attribute_modifiers` component, so stacks fall back to the
  modifiers that `ArmorItem` derives from its material. For gold armor with an empty component, the mod writes
  that material-derived list into the component with only the `armor` value changed. Toughness and any other
  modifier are copied as they are. Gold armor that already defines its own modifiers is left alone.
- **Durability** follows the vanilla gold materials (`ToolMaterials.GOLD`, `ArmorMaterials.GOLD`), as before.
- Enchantability, mining speed, repair material and the materials themselves are not touched.

Side effects:

- **Mobs use the same items.** Zombified piglins always carry a golden sword and many piglins do, so their
  melee damage rises from 8 to 10.5 before difficulty scaling. Piglin brutes' golden axes go from 13 to 14.
  Mobs that spawn wearing gold armor are tougher. This keeps items consistent: a sword hits as hard as its
  tooltip says, whoever holds it.
- **Mob armor pickup** compares `ArmorItem.getProtection()`, which still reads the material (vanilla gold
  values). A mob wearing iron may therefore ignore a golden piece that is actually better. It only affects which
  armor mobs pick up from the ground.

### Villager diamond trades (`trade/DiamondGearTrades`)

Diamond gear from villagers lets players skip mining entirely. Removing the trades would be a nerf, so each
diamond-gear offer instead asks for a second payment: **half of the item's crafting diamonds, rounded up,
minimum 1**.

| Item | Diamonds asked |
| --- | --- |
| Helmet | 3 |
| Chestplate | 4 |
| Leggings | 4 |
| Boots | 2 |
| Pickaxe, axe | 2 |
| Sword, hoe, shovel | 1 |

The villager still saves the player half the diamonds plus the enchantment, and the emerald price is unchanged.

How it works:

- The factories in `TradeOffers.PROFESSION_TO_LEVELED_TRADE` are wrapped for armorers, toolsmiths and
  weaponsmiths only.
- Offers that already use their second buy slot, or that are paid in diamonds, are left alone.
- Offers already generated for existing villagers are **not** changed.

### Enchanting pages (`enchantment/EnchantingPages`, `mixin/EnchantmentScreenHandlerMixin`)

Vanilla shows three offers and gives no reason to put more than 3 lapis in the table. With pages:

- `page = min((lapis - 1) / 3, maxPages - 1)`.
- Page `p`, slot `s` costs `3p + s + 1` lapis. The XP cost of an enchantment stays vanilla.
- Page 1 (1–3 lapis) is exactly vanilla: its seed is the unmodified player seed.
- Other pages generate offers with `seed + page × 0x9E3779B97F4A7C15`. This is deterministic, so players cannot
  reroll by moving lapis in and out. Enchanting changes the player's seed exactly as vanilla does.
- The page is computed on the server and synced as a screen handler property.
- The client mixin (`EnchantmentScreenMixin`) makes the vanilla screen display the page's lapis cost and adds an
  "Offer page N" tooltip line. The screen itself is not replaced.
- The server enforces the page cost in `onButtonClick`. This check does not rely on the client.

Full stacks of lapis land on the last page (7–9 by default), which is intended: more lapis means more offers.

### Enchantments (`enchantment/EnchantmentRebalance`, tags, `mixin/DamageEntityEnchantmentEffectMixin`)

The design rule:

> General-purpose enchantments should be convenient and reliable. Specialized enchantments should be
> substantially stronger when their specialization applies.

Vanilla breaks this rule in a few places. Protection IV is so strong that the specialized protections are never
worth a slot, Bane of Arthropods covers too few mobs to be worth it over Sharpness, Thorns deals little and its
reflected hit can eat into the attacker's next hit, and Impaling does nothing outside the ocean. The mod changes
only these four enchantments. It adds no enchantment and keeps every vanilla id.

In 1.21.1 enchantments are data. `EnchantmentRebalance` edits the built-in definitions while they load, through
Fabric's `EnchantmentEvents.MODIFY`. The event hands over a builder pre-filled with the vanilla effects; its
effect lists are reached with the `EnchantmentBuilderAccessor` invoker. Only builtin sources (vanilla or
mod-bundled) are edited, so an enchantment replaced by a data pack is used exactly as the pack defines it.
Effects are evaluated on the server only, so the edits have no gameplay effect on clients.

#### Protection

Vanilla adds up the protection points of every piece, caps the total at 20 and reduces damage by 4% per point
(`DamageUtil.getInflictedDamage`). Protection gives 1 point per level against almost everything; Fire, Blast
and Projectile Protection give 2 points per level against their damage type.

The mod scales only Protection's `damage_protection` value by `protectionReductionPerLevel / 4`: with the
default 3%, 0.75 points per level. Everything else is vanilla, including the cap and the conditions (Protection
still does not reduce damage that bypasses invulnerability). With four level-IV pieces, through vanilla's formula:

| Armor | General damage | Matching damage type |
| --- | --- | --- |
| 4× Protection | 48% (vanilla 64%) | 48% |
| 3× Protection + 1 specialized | 36% | 68% |
| 2× Protection + 2 specialized | 24% | 80% (cap) |
| 4× specialized | 0% | 80% (cap) |

These numbers are exact. They are checked by a unit test through `DamageUtil` and by a GameTest with
enchanted armor. The reductions above come from enchantments only; armor points and toughness apply first
and are unchanged. Feather Falling was not touched.

#### Bane

The display name becomes "Bane" (a lang override for `enchantment.minecraft.bane_of_arthropods`, in English and
Spanish); the id stays `minecraft:bane_of_arthropods`. Existing items, commands and data packs keep working.

Vanilla Bane has two effects: +2.5 damage per level and Slowness IV on hit, both requiring the target to be in
`#minecraft:sensitive_to_bane_of_arthropods`. That tag contains `#minecraft:arthropod` and is used by nothing
else. So Bane needs no code:

- The new tag `#progressionrebalance:bane_targets` lists spider, cave spider, silverfish, endermite, creeper,
  slime, magma cube, guardian, elder guardian and ravager.
- The mod *appends* `#progressionrebalance:bane_targets` to the vanilla tag (`replace: false`). The vanilla
  arthropods, including bees, stay. The damage scaling and the Slowness effect are the vanilla ones and now also
  apply to the new targets.
- Mods and data packs extend it by adding entries to `data/progressionrebalance/tags/entity_type/bane_targets.json`
  (or to the vanilla tag). It is not a config option.

Creepers, slimes, guardians and ravagers are dangerous in close combat or have a lot of health, which gives a
dedicated weapon a real niche. Zombies, skeletons and other undead stay Smite's; humanoids like pillagers and
witches stay Sharpness's.

#### Thorns

Vanilla Thorns is a `post_attack` effect: with a 15% chance per level it deals 1–5 `minecraft:thorns` damage
(uniform) to the attacker and costs 2 extra durability. The mod scales the damage range by
`thornsDamageMultiplier` (1.35 → 1.35–6.75). The chance and durability cost are unchanged.

**Damage cooldown ("i-frames").** In `LivingEntity.damage`:

- A hit landing while `timeUntilRegen > 10` only deals the part above `lastDamageTaken`, or nothing.
- Any other hit sets `timeUntilRegen = 20` and `lastDamageTaken`.

So vanilla Thorns has two problems. It *starts* a cooldown on the attacker, which blocks or reduces the
defender's counter-attack for half a second. And it is *absorbed* when the attacker is already in a cooldown.

The fix is targeted to the enchantment effect:

- `DamageEntityEnchantmentEffectMixin` wraps the single `Entity.damage` call inside
  `DamageEntityEnchantmentEffect.apply`, the effect type that Thorns uses.
- It only acts when the damage type is in `#progressionrebalance:ignores_hurt_cooldown` (contains
  `minecraft:thorns`) and the target is a `LivingEntity`. Every other damage enchantment effect, including other
  mods' effects, goes through untouched.
- `HurtCooldown.runOutside` saves `timeUntilRegen` and `lastDamageTaken`, clears the cooldown, deals the damage
  through the normal `damage()` call, and restores both fields in a `finally` block.
- Health, armor, the hurt animation and sound, death, knockback rules and attacker tracking are all vanilla. Only
  the cooldown fields are put back as they were.

Thorns damage therefore neither sets nor consumes a cooldown, in both directions. A sword hit right after a
Thorns reflection deals full damage, and a Thorns reflection during an existing cooldown is not absorbed. The
cooldown fields are never changed for any other damage, and there is no global i-frame change.

Other safety points:

- **No recursion.** Thorns runs from `EnchantmentHelper.onTargetDamaged`, which is only called by attack code
  (melee, projectiles, some mob attacks), never by `damage()` itself. The reflected hit therefore cannot trigger
  Thorns on the original attacker, even if they wear Thorns too.
- **No double damage.** The original damage call happens exactly once, with its original arguments.
- **No desync.** It all runs on the server. Health and the hurt animation sync to clients as usual.
- **Guardian spikes** also use the `minecraft:thorns` damage type, but the guardian calls `damage()` directly
  rather than through the enchantment effect. They keep vanilla i-frames, which a GameTest checks.

#### Impaling

Vanilla Impaling adds +2.5 damage per level against `#minecraft:sensitive_to_impaling` (aquatic mobs). The mod
adds a **companion** damage entry for each conditional bonus:

- The value is the original value × `impalingWetMultiplier` (0.6 → +1.5 per level, +7.5 at Impaling V).
- The condition is *not* the original condition *and* `progressionrebalance:is_wet`. An aquatic target
  always gets the full bonus and never both.
- `progressionrebalance:is_wet` is a small loot condition registered by the mod. It is true when
  `Entity.isWet()` is true for the target. This is vanilla's own predicate: touching water, being rained on
  (open sky above and a biome where it rains, not snow) or standing in a bubble column.
- Dry, non-aquatic targets get no bonus. Setting the multiplier to 0 skips the companion entirely.

This makes Impaling a real choice for tridents in rainy weather and around water, while keeping its full bonus
for the ocean.

### Minecarts (`transport/MinecartSpeed`, `mixin/AbstractMinecartEntityMixin`)

The multiplier is applied to the value of `getMaxSpeed()` inside `moveOnRail` only:

- Off-rail movement and the furnace minecart's push force keep vanilla physics.
- Every on-rail cap that `getMaxSpeed()` returns is multiplied: the halved cap in water (4 → 10 blocks/s) and
  the furnace minecart's lower cap (4 → 10 blocks/s) scale like the normal one.

The default 2.5 gives 1 block per tick (20 blocks/s), which is also the config maximum. Two limits keep carts on
the track:

- **Straight rails and slopes.** At up to 1 block per tick a cart still stops in every rail block, so it cannot
  skip over a curve or a slope.
- **Curves** keep the vanilla clamp. On a curve vanilla moves the cart diagonally, up to the clamp on *both* axes.
  Above half a block per axis the cart can end up in the diagonal neighbour block, where there is no rail.
  Vanilla's 0.4 is safe from any entry point, while even 0.68 derails from some entry points. The cart keeps its
  velocity through the curve and is back at full speed on the next straight block.

GameTests cover straight speed, four curves entered at different points, and full-speed climbs and descents.
A GameTest also confirms that, without the curve rule, carts derail.

Minecart movement is simulated on the server in 1.21.1. The client only interpolates, so no client change is needed.

### Rails (`recipe/RailRecipeOutputs`, `mixin/RecipeManagerMixin`, datagen)

Rails are gated by iron, which pushes long rail lines to the late game. Copper is plentiful and otherwise
underused.

- **Copper rails.** The recipe is 6 copper ingots + 1 stick → 8 rails. It uses the
  `c:ingots/copper` tag and is generated by datagen.
- **Powered rails.** Output goes from 6 to 12.
- **Iron rails.** Vanilla already gives 16. The value is exposed so servers can tune it.

Outputs are applied when recipes (re)load, only if the recipe is still a shaped recipe with the expected result
item. A data pack that replaces one of these recipes is left alone.

### Potion durations (`potion/PotionDurationPolicy`, `mixin/PotionsMixin`)

Each brewed potion falls into the first matching category:

| Category | Rule | Default multiplier |
| --- | --- | --- |
| Unchanged | No timed effects (e.g. healing) | – |
| Amplified | Any effect at level II+ | ×1.0 |
| Combat | Contains a `combatEffects` effect | ×1.5 |
| Harmful | All effects harmful | ×1.0 |
| Utility | Everything else | ×2.0 |

- Level II and combat potions are deliberately conservative. Longer Strength II or Regeneration II would make PvP
  and boss fights easier. Utility effects (night vision, fire resistance, water breathing, swiftness, slow falling,
  invisibility, leaping, luck) are what make exploration fun.
- The policy runs when vanilla registers each potion, so every derived item (splash, lingering, tipped arrows,
  potion tooltips) sees the same durations.
- Tipped arrows keep vanilla's 1/8 scaling.

### Potion stacking (`potion/PotionStacking`)

Stacking is kept conservative. Only **drinkable** potions stack, to 4. Splash and
lingering potions are throwable weapons, and stacking them would be a combat buff.

### Potion cauldrons (`potion/PotionCauldrons`, `potion/PotionCauldronBlock`, `potion/PotionMixing`)

How to mix:

1. Use a potion on a water cauldron (any level). The potion is poured in, you get the empty bottle back, and the
   cauldron becomes a **potion cauldron** whose liquid takes the potion's color.
2. Pour more potions of the **same kind** into it. Kinds are drinkable, splash and lingering.
3. Use an **empty bottle** on it. The whole mixture comes out as one potion, and one water level is used. The
   remaining water turns back into plain water; the last level leaves an empty cauldron.

Rules:

- Effects are merged by type. When two potions share an effect, the stronger instance wins: higher amplifier
  first, then longer duration. Durations and amplifiers are **never added**, and amplifiers never change.
- **Durations are averaged** (`potion/DurationContributions`). Every timed effect of the mixture lasts
  `totalTicks / contributors`, rounded to the nearest tick:
  - An ordinary potion is one contributor with its duration. It was already adjusted by the duration policy, so
    Long Night Vision counts as 16:00.
  - A mixture keeps the **sum and the count** of all its contributions, never only the average. Pouring a 3:00
    potion into a 16:00 + 12:00 mixture gives (16 + 12 + 3) / 3 = 10:20, not (14 + 3) / 2 = 8:30.
  - Bottled mixed potions store the sum and count in the vanilla `minecraft:custom_data` component under
    `progressionrebalance:mixed_durations` (`total_ticks`, `contributors`). It is saved, synced and moved with the
    stack like any item data. A registered component type was avoided on purpose: removing the mod would make
    stacks with an unknown component fail to load. When a mixed potion is poured in, its stored contributions
    are added, so chained mixing stays exact in either order.
  - Potion cauldrons store the same pair in their block entity (`durations`). Cauldrons saved before this
    change count their contents as one contributor.
  - Instant effects (`StatusEffect#isInstant`) and infinite effects take no part: they contribute nothing and
    keep their own duration. A potion with only instant effects (Healing) adds no contributor.
  - A potion whose timed effects have different durations (not possible with vanilla potions) contributes the
    average of its own effects, as one contributor.
  - Invalid stored data (e.g. edited by hand) is ignored, and the potion counts as one contributor.
  - Two mixed potions with identical effects but different ingredients (e.g. 16 + 12 and 14 + 14) have different
    data, so they do not stack with each other.
- Each poured potion must add at least one new effect, and the mixture must stay within `maxMixedEffects` (3).
- Rejected potions show a red action-bar message and are kept. They are not drunk instead.
- All the potions poured in come out as a **single** potion, so mixing can never duplicate effects. This keeps
  the old "N potions in, 1 out" balance.
- A single poured potion comes back unchanged, with its vanilla name.

Naming:

- A mixture has no base potion, so vanilla would call it "Uncraftable Potion". Mixtures get an item name
  component instead: "Mixed Potion", "Mixed Splash Potion" or "Mixed Lingering Potion". It is not a custom
  name, so it isn't italic and anvils treat it as the default name.
- The name is a translation with an English fallback, so it works in every language and even on clients
  without the mod's language file. English and Spanish translations are included.
- Tipped arrows crafted from a mixed lingering potion only copy its effects. Their name comes from overriding
  vanilla's "Uncraftable Tipped Arrow" key.

Implementation notes:

- **The block.** `progressionrebalance:potion_cauldron` is a leveled cauldron with a block entity that stores
  the kind and the potion contents.
  - It reuses the vanilla water cauldron models and loot table, so breaking it drops a cauldron.
  - Rain and dripstone do not fill it.
  - It is in `#minecraft:cauldrons` and gives off faint particles in the mixture's color.
- **Syncing.** The block entity syncs to clients. A Fabric block color provider tints the liquid using Fabric's
  block-entity render data, which is Sodium-compatible.
- **Interactions.**
  - Pouring into a *water* cauldron uses `UseBlockCallback`, because vanilla rebuilds its water-cauldron
    behavior map during bootstrap (after mod init).
  - The potion cauldron has its own behavior map.
  - The server decides everything. The client only predicts the result for arm swings.

### Structure loot (`loot/StructureLoot`)

Every addition is a separate single-roll pool with a large "nothing" weight. Nothing is guaranteed and no vanilla
entry is removed. Additions are thematic:

| Structure | Additions |
| --- | --- |
| Mineshafts | Minecarts, lanterns, night vision |
| Fortresses | Blaze powder, magma cream, glowstone, fire resistance, strength |
| Bastions | Fire resistance, strength. Treasure room adds long regeneration and Resistance |
| Ruined portals | Fire resistance |
| Shipwrecks, ocean ruins, buried treasure | Water breathing, night vision, spyglass, compass, rare Luck |
| Woodland mansions | Invisibility, strength, regeneration |
| Ancient cities | Night vision, invisibility, Resistance, lapis, bottles o' enchanting |
| End cities | Slow falling, rare Luck |
| Dungeons, temples, strongholds | Early utility potions |
| Witches | Rare brewed potion on player kills |

Witch huts have no chest in Java Edition, so the witch's own loot table is used. Looting improves the drop
chance slightly.

**Resistance** has no vanilla potion. The mod registers one potion entry (`progressionrebalance:resistance`,
Resistance I for 3:00) that is only obtainable from loot. It is not brewable.

### Luck (`loot/LuckLoot`, `loot/LuckBonusRollsProvider`)

In vanilla, Luck only affects fishing and a handful of `quality` entries, so the Luck potion is nearly worthless.
The mod gives it a controlled effect:

- For each eligible pool, a **copy of the pool** is added whose roll count is
  `min(luck, maxEffectiveLuck) × rollsPerLuck`. The fractional part is rolled.
- With the default 0.5, Luck I gives every chest pool a 50% chance of one extra roll, and Luck II guarantees one.
- The cap is luck 4. Negative luck gives zero extra rolls and never *removes* loot.

Eligible pools:

- **Chests:** every pool of `minecraft:chests/*`, except trial chamber rewards (their own balance and ominous
  keys), the spawn bonus chest and the jungle temple dispenser.
- **Mob drops:** only pools that have both a *killed by player* condition and a *random chance* condition,
  i.e. rare drops like zombie iron or wither skeleton skulls. Common drops and guaranteed drops are unaffected.

Deliberately skipped:

- **Fishing** already uses Luck through vanilla's treasure quality.
- **Archaeology** only keeps the first generated item, so extra rolls do nothing.

Without luck, the provider returns before using the loot RNG. Seeded chest loot is therefore identical to
running without the mod. Mob drops keep vanilla odds; they are unseeded anyway.

## Things intentionally not changed

- Netherite, diamond and iron balance; mining speeds; the combat stats of golden pickaxes, shovels and hoes.
- Enchantment XP costs, the enchanting screen layout, and every enchantment other than Protection, Bane,
  Thorns and Impaling. No enchantment is added or renamed at the id level.
- Global damage cooldown (i-frames) behavior. Only Thorns enchantment damage is exempt.
- Minecart off-rail physics, boats, horses and elytra.
- Brewing recipes. No new brewable potions.
- Trial chambers, fishing and archaeology loot.
- Existing villager offers.

## Compatibility

- **Loot.** Only `LootTableEvents.MODIFY` is used, and only for built-in tables (`source.isBuiltin()`). A data
  pack that replaces a table wins completely.
- **Recipes.** Output counts are changed only on the exact vanilla shaped recipe with the expected result item.
- **Enchantments.** `EnchantmentEvents.MODIFY`, only for builtin definitions. Data packs that replace Protection,
  Thorns or Impaling win completely. Bane targets are a tag that is appended to vanilla's, never replaced.
  Mods that reimplement damage reduction, or that read the Protection effect themselves, see the new value.
- **Potions.** Durations are rewritten at registration. Mods that register potions through their own code are
  unaffected. Mods that read vanilla potion effects see the new durations.
- **Mixins.** Each one targets a single call site with `@ModifyExpressionValue`, `@ModifyArg`, `@WrapOperation`
  or HEAD/TAIL injects, never `@Overwrite`:
  - `AbstractMinecartEntityMixin`: `getMaxSpeed()` inside `moveOnRail`, with the rail shape read from the method's
    arguments. Other minecart speed mods will stack multiplicatively. Mods that replace minecart movement
    entirely bypass it.
  - `EnchantmentScreenHandlerMixin`: page property, seed and lapis charge. Mods that replace the enchanting table
    entirely (e.g. Enchanting Infuser, Apotheosis-style) will conflict.
  - `EnchantmentScreenMixin` (client only): lapis number display and tooltip.
  - `EnchantmentBuilderAccessor`: invoker for the builder's effect lists used by the MODIFY event.
  - `DamageEntityEnchantmentEffectMixin` / `LivingEntityAccessor`: `@WrapOperation` on the one `damage()` call in
    the enchantment damage effect, for damage types in `#progressionrebalance:ignores_hurt_cooldown` only. Mods
    that also change i-frames (e.g. "no i-frames" combat mods) are compatible: the fields are restored to
    whatever they were before the call.
  - `PotionsMixin` / `PotionAccessor`: rewrite potion effect durations at registration.
  - `RecipeManagerMixin` / `ShapedRecipeAccessor`: set rail output counts after recipes load.
- **Client/server.** The mod must be installed on both sides. It registers a block (the potion cauldron) and a
  potion, which Fabric's registry sync requires the client to know. The enchanting table also syncs an extra
  screen handler property (the page), and the page display and cauldron tint are client code. Use the same
  config on both sides: stack sizes, durability, weapon and armor stats and potion durations are item and
  registry defaults that each side computes for itself.
