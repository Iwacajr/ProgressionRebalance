package com.iwaca.progressionrebalance.potion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PotionMixingTest {
    private static final int MAX = 3;
    private static final int MINUTE = 60 * 20;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    private static ItemStack potion(Item kind, RegistryEntry<Potion> potion) {
        return PotionContentsComponent.createStack(kind, potion);
    }

    private static ItemStack potion(RegistryEntry<Potion> potion) {
        return potion(Items.POTION, potion);
    }

    /** A drinkable potion with explicit effects, like a potion from another mod or a command. */
    private static ItemStack potionWith(StatusEffectInstance... effects) {
        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.empty(), List.of(effects)));
        return stack;
    }

    private static StatusEffectInstance effect(RegistryEntry<StatusEffect> type, int ticks) {
        return new StatusEffectInstance(type, ticks);
    }

    private static List<StatusEffectInstance> effects(ItemStack stack) {
        return StreamSupport.stream(stack.get(DataComponentTypes.POTION_CONTENTS).getEffects().spliterator(), false).toList();
    }

    private static StatusEffectInstance find(ItemStack stack, RegistryEntry<StatusEffect> type) {
        return effects(stack).stream().filter(e -> e.getEffectType().equals(type)).findFirst().orElseThrow();
    }

    /** Pours {@code poured} into a cauldron that holds {@code inCauldron}, as a potion cauldron does. */
    private static PotionMixing.Result mix(ItemStack inCauldron, ItemStack poured, int max) {
        return PotionMixing.pour(inCauldron.getItem(), PotionMixing.effectsOf(inCauldron), DurationContributions.of(inCauldron), poured, max);
    }

    private static ItemStack mixed(PotionMixing.Result result) {
        assertTrue(result.isSuccess(), () -> "expected success but was " + result.rejection());
        return PotionMixing.createMixedPotion(Items.POTION, result.effects(), result.durations());
    }

    @Test
    void twoPotionDurationsAreAveraged() {
        ItemStack mixture = mixed(mix(potionWith(effect(StatusEffects.NIGHT_VISION, 16 * MINUTE)),
                potionWith(effect(StatusEffects.FIRE_RESISTANCE, 12 * MINUTE)), MAX));
        for (StatusEffectInstance effect : effects(mixture)) {
            assertEquals(14 * MINUTE, effect.getDuration(), "(16 + 12) / 2 = 14:00");
        }
        assertEquals(new DurationContributions(28L * MINUTE, 2), DurationContributions.of(mixture));
    }

    @Test
    void threePotionDurationsAreAveraged() {
        ItemStack two = mixed(mix(potionWith(effect(StatusEffects.NIGHT_VISION, 16 * MINUTE)),
                potionWith(effect(StatusEffects.FIRE_RESISTANCE, 12 * MINUTE)), MAX));
        ItemStack three = mixed(mix(two, potionWith(effect(StatusEffects.WATER_BREATHING, 3 * MINUTE)), MAX));
        assertEquals(3, effects(three).size());
        for (StatusEffectInstance effect : effects(three)) {
            assertEquals(10 * MINUTE + 20 * 20, effect.getDuration(), "(16 + 12 + 3) / 3 = 10:20, not (14 + 3) / 2 = 8:30");
        }
    }

    @Test
    void anAlreadyMixedPotionCountsEveryPotionInIt() {
        ItemStack mixture = mixed(mix(potionWith(effect(StatusEffects.NIGHT_VISION, 16 * MINUTE)),
                potionWith(effect(StatusEffects.FIRE_RESISTANCE, 12 * MINUTE)), MAX));
        // Poured the other way round: a 3:00 potion in the cauldron, then the bottled 14:00 mixture.
        ItemStack result = mixed(mix(potionWith(effect(StatusEffects.WATER_BREATHING, 3 * MINUTE)), mixture, MAX));
        assertEquals(10 * MINUTE + 20 * 20, find(result, StatusEffects.NIGHT_VISION).getDuration());
        assertEquals(new DurationContributions(31L * MINUTE, 3), DurationContributions.of(result));

        // The contributions survive copying the stack and re-reading its custom data.
        assertEquals(DurationContributions.of(result), DurationContributions.of(result.copy()));
    }

    @Test
    void instantEffectsStayInstantAndAreNotAveraged() {
        StatusEffectInstance healingII = new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 1);
        ItemStack two = mixed(mix(potionWith(effect(StatusEffects.NIGHT_VISION, 16 * MINUTE)),
                potionWith(effect(StatusEffects.FIRE_RESISTANCE, 8 * MINUTE)), MAX));
        ItemStack three = mixed(mix(two, potionWith(healingII), MAX));

        assertEquals(12 * MINUTE, find(three, StatusEffects.NIGHT_VISION).getDuration(), "(16 + 8) / 2, healing adds no 0:00");
        assertEquals(12 * MINUTE, find(three, StatusEffects.FIRE_RESISTANCE).getDuration());
        StatusEffectInstance healing = find(three, StatusEffects.INSTANT_HEALTH);
        assertEquals(1, healing.getAmplifier(), "Instant Health II stays II");
        assertEquals(1, healing.getDuration(), "instant effects keep their vanilla instant duration");
        assertEquals(2, DurationContributions.of(three).contributors(), "the healing potion is not a duration contributor");

        assertEquals(DurationContributions.NONE, DurationContributions.of(potion(Potions.STRONG_HEALING)));
    }

    @Test
    void amplifiersAreNeverChanged() {
        ItemStack mixture = mixed(mix(potionWith(new StatusEffectInstance(StatusEffects.STRENGTH, 90 * 20, 1)),
                potionWith(new StatusEffectInstance(StatusEffects.SPEED, 6 * MINUTE, 0)), MAX));
        StatusEffectInstance strength = find(mixture, StatusEffects.STRENGTH);
        StatusEffectInstance speed = find(mixture, StatusEffects.SPEED);
        assertEquals(1, strength.getAmplifier(), "Strength II stays Strength II");
        assertEquals(0, speed.getAmplifier(), "Speed I stays Speed I");
        assertEquals((90 * 20 + 6 * MINUTE) / 2, strength.getDuration());
        assertEquals(strength.getDuration(), speed.getDuration());
    }

    @Test
    void duplicatesKeepTheStrongerAmplifierAndEveryPotionCountsTowardsTheAverage() {
        StatusEffectInstance strongShort = new StatusEffectInstance(StatusEffects.STRENGTH, 1800, 1);
        StatusEffectInstance weakLong = new StatusEffectInstance(StatusEffects.STRENGTH, 9600, 0);
        StatusEffectInstance nightVision = new StatusEffectInstance(StatusEffects.NIGHT_VISION, 9600, 0);

        PotionMixing.Result result = PotionMixing.mix(List.of(strongShort), DurationContributions.ofPotionEffects(List.of(strongShort)),
                List.of(weakLong, nightVision), DurationContributions.ofPotionEffects(List.of(weakLong, nightVision)), MAX);
        assertTrue(result.isSuccess());
        StatusEffectInstance strength = result.effects().getFirst();
        assertEquals(1, strength.getAmplifier(), "the stronger level wins; levels are never combined");
        assertEquals((1800 + 9600) / 2, strength.getDuration(), "durations are averaged, never added");
        assertEquals(strength.getDuration(), result.effects().get(1).getDuration());
    }

    @Test
    void potionsWithTheSameDurationKeepIt() {
        ItemStack two = mixed(mix(potion(Potions.LONG_SWIFTNESS), potion(Potions.LONG_NIGHT_VISION), MAX));
        ItemStack three = mixed(mix(two, potion(Potions.LONG_FIRE_RESISTANCE), MAX));
        int expected = Potions.LONG_SWIFTNESS.value().getEffects().getFirst().getDuration();
        for (StatusEffectInstance effect : effects(three)) {
            assertEquals(expected, effect.getDuration(), "8:00 potions all share one (rebalanced) duration");
            assertEquals(0, effect.getAmplifier());
        }
        PotionContentsComponent contents = three.get(DataComponentTypes.POTION_CONTENTS);
        assertTrue(contents.potion().isEmpty(), "mixed potions have no base potion, so they cannot be brewed further");
        assertNotEquals(-1, contents.getColor());
    }

    @Test
    void storedContributionsRoundTripAndBadDataIsIgnored() {
        DurationContributions contributions = new DurationContributions(37200L, 3);
        assertEquals(Optional.of(contributions), DurationContributions.fromNbt(contributions.toNbt()));
        assertEquals(12400, contributions.averageTicks());

        NbtCompound broken = new NbtCompound();
        broken.putLong("total_ticks", 100L);
        broken.putInt("contributors", 0);
        assertEquals(Optional.empty(), DurationContributions.fromNbt(broken));

        // A potion whose stored data was damaged counts as one potion with its own duration.
        ItemStack stack = potionWith(effect(StatusEffects.NIGHT_VISION, 5 * MINUTE));
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.put(DurationContributions.CUSTOM_DATA_KEY, broken));
        assertEquals(new DurationContributions(5L * MINUTE, 1), DurationContributions.of(stack));
    }

    @Test
    void aFourthEffectIsRejected() {
        ItemStack three = mixed(mix(
                mixed(mix(potion(Potions.SWIFTNESS), potion(Potions.NIGHT_VISION), MAX)),
                potion(Potions.FIRE_RESISTANCE), MAX));
        PotionMixing.Result result = mix(three, potion(Potions.WATER_BREATHING), MAX);
        assertEquals(PotionMixing.Rejection.TOO_MANY_EFFECTS, result.rejection());
        assertTrue(mix(three, potion(Potions.WATER_BREATHING), 4).isSuccess());
    }

    @Test
    void mixingMustAddANewEffect() {
        PotionMixing.Result result = mix(potion(Potions.SWIFTNESS), potion(Potions.LONG_SWIFTNESS), MAX);
        assertEquals(PotionMixing.Rejection.NO_NEW_EFFECT, result.rejection());
    }

    @Test
    void potionsWithoutEffectsAndDifferentKindsAreRejected() {
        assertEquals(PotionMixing.Rejection.NOTHING_TO_MIX,
                mix(potion(Potions.WATER), potion(Potions.SWIFTNESS), MAX).rejection());
        assertEquals(PotionMixing.Rejection.NOTHING_TO_MIX,
                mix(potion(Potions.SWIFTNESS), potion(Potions.AWKWARD), MAX).rejection());
        assertEquals(PotionMixing.Rejection.DIFFERENT_KINDS,
                mix(potion(Potions.SWIFTNESS), potion(Items.SPLASH_POTION, Potions.NIGHT_VISION), MAX).rejection());
        assertEquals(PotionMixing.Rejection.NOTHING_TO_MIX,
                mix(potion(Potions.SWIFTNESS), new ItemStack(Items.GLASS_BOTTLE), MAX).rejection());
    }

    @Test
    void splashAndLingeringPotionsMixWithTheirOwnKind() {
        for (Item kind : List.of(Items.SPLASH_POTION, Items.LINGERING_POTION)) {
            PotionMixing.Result result = mix(potion(kind, Potions.SWIFTNESS), potion(kind, Potions.NIGHT_VISION), MAX);
            assertTrue(result.isSuccess());
            assertTrue(PotionMixing.createMixedPotion(kind, result.effects(), result.durations()).isOf(kind));
        }
    }

    @Test
    void mixedPotionsHaveAMixedPotionName() {
        ItemStack two = mixed(mix(potion(Potions.SWIFTNESS), potion(Potions.NIGHT_VISION), MAX));
        Text name = two.get(DataComponentTypes.ITEM_NAME);
        assertTrue(name != null && name.getContent() instanceof TranslatableTextContent content
                && content.getKey().equals("item.progressionrebalance.mixed_potion"), () -> "name was " + name);
        assertEquals(name, two.getName(), "the item name replaces vanilla's \"Uncraftable Potion\"");
        assertNull(two.get(DataComponentTypes.CUSTOM_NAME), "not a custom (italic, anvil) name");

        ItemStack splash = PotionMixing.createMixedPotion(Items.SPLASH_POTION, effects(two), DurationContributions.of(two));
        assertEquals("Mixed Splash Potion", splash.getName().getString());
    }

    @Test
    void bottlingASinglePotionGivesItBackUnchanged() {
        ItemStack swiftness = potion(Potions.SWIFTNESS);
        ItemStack bottled = PotionMixing.bottle(Items.POTION, swiftness.get(DataComponentTypes.POTION_CONTENTS), DurationContributions.of(swiftness));
        assertTrue(ItemStack.areEqual(swiftness, bottled), "one poured potion comes back as the same potion");
        assertNull(bottled.get(DataComponentTypes.ITEM_NAME));

        List<StatusEffectInstance> nightVision = effects(potion(Potions.NIGHT_VISION));
        PotionContentsComponent mixture = new PotionContentsComponent(Optional.empty(), Optional.empty(), nightVision);
        ItemStack mixedBottle = PotionMixing.bottle(Items.LINGERING_POTION, mixture, DurationContributions.ofPotionEffects(nightVision));
        assertTrue(mixedBottle.isOf(Items.LINGERING_POTION));
        assertEquals("Mixed Lingering Potion", mixedBottle.getName().getString());

        ItemStack empty = PotionMixing.bottle(Items.POTION, PotionContentsComponent.DEFAULT, DurationContributions.NONE);
        assertTrue(empty.get(DataComponentTypes.POTION_CONTENTS).matches(Potions.WATER), "a cauldron without effects gives water");
    }

    @Test
    void inputsAreNotModified() {
        ItemStack base = potion(Potions.SWIFTNESS);
        ItemStack addition = potion(Potions.LONG_NIGHT_VISION);
        int before = effects(base).getFirst().getDuration();
        mix(base, addition, MAX);
        assertEquals(1, effects(base).size());
        assertEquals(before, effects(base).getFirst().getDuration());
        assertFalse(base.get(DataComponentTypes.POTION_CONTENTS).potion().isEmpty());
    }
}
