package com.iwaca.progressionrebalance.potion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/**
 * Side-independent rules for mixing potions in a {@link PotionCauldronBlock potion cauldron}.
 *
 * <ul>
 *     <li>Every poured potion must be the same kind (drinkable, splash or lingering) and have at least one effect.</li>
 *     <li>Effects are merged per effect type. For a duplicated effect the stronger instance wins: the higher
 *     amplifier (on equal amplifiers, the longer duration). Amplifiers are never raised or combined.</li>
 *     <li>Every timed effect of the result lasts the <b>average</b> of the original durations of all potions
 *     poured in ({@link DurationContributions}). Instant effects stay instant and take no part in the average.
 *     Durations never add up.</li>
 *     <li>The added potion must contribute at least one new effect type, so mixing never just wastes a potion.</li>
 *     <li>The result may hold at most the configured number of distinct effects.</li>
 * </ul>
 * The result keeps no base potion, only explicit effects, so it cannot be brewed further (vanilla brewing
 * recipes match on the base potion). Splash and lingering mixtures are made from splash and lingering potions;
 * lingering mixtures can still be crafted into tipped arrows.
 */
public final class PotionMixing {
    public enum Rejection {
        NOTHING_TO_MIX("message.progressionrebalance.mixing.nothing_to_mix"),
        DIFFERENT_KINDS("message.progressionrebalance.mixing.different_kinds"),
        NO_NEW_EFFECT("message.progressionrebalance.mixing.no_new_effect"),
        TOO_MANY_EFFECTS("message.progressionrebalance.mixing.too_many_effects");

        private final String translationKey;

        Rejection(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }

    /** Either the merged effects with their duration contributions, or the reason mixing is not allowed. */
    public record Result(List<StatusEffectInstance> effects, DurationContributions durations, @Nullable Rejection rejection) {
        static Result success(List<StatusEffectInstance> effects, DurationContributions durations) {
            return new Result(List.copyOf(effects), durations, null);
        }

        static Result rejected(Rejection rejection) {
            return new Result(List.of(), DurationContributions.NONE, rejection);
        }

        public boolean isSuccess() {
            return rejection == null;
        }
    }

    /** The potion kinds that can be poured into a cauldron. A mixture only ever holds one kind. */
    public static final List<Item> KINDS = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

    private PotionMixing() {
    }

    public static boolean isMixablePotionItem(ItemStack stack) {
        return KINDS.contains(stack.getItem());
    }

    public static boolean hasEffects(ItemStack stack) {
        PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
        return contents != null && contents.hasEffects();
    }

    /**
     * Pours {@code poured} into a cauldron that already holds {@code current} potion of the given kind, whose
     * durations came from {@code currentDurations}.
     */
    public static Result pour(Item cauldronKind, Iterable<StatusEffectInstance> current, DurationContributions currentDurations,
            ItemStack poured, int maxEffects) {
        if (!isMixablePotionItem(poured) || !hasEffects(poured)) {
            return Result.rejected(Rejection.NOTHING_TO_MIX);
        }
        if (!poured.isOf(cauldronKind)) {
            return Result.rejected(Rejection.DIFFERENT_KINDS);
        }
        return mix(current, currentDurations, effectsOf(poured), DurationContributions.of(poured), maxEffects);
    }

    /**
     * Merges two sets of effects, then gives every timed effect the average duration of all contributions. The
     * contributions add up, so a mixture poured into another counts every potion that went into it.
     */
    public static Result mix(Iterable<StatusEffectInstance> base, DurationContributions baseDurations,
            Iterable<StatusEffectInstance> addition, DurationContributions additionDurations, int maxEffects) {
        Map<RegistryEntry<StatusEffect>, StatusEffectInstance> merged = new LinkedHashMap<>();
        for (StatusEffectInstance effect : base) {
            merged.merge(effect.getEffectType(), new StatusEffectInstance(effect), PotionMixing::stronger);
        }
        if (merged.isEmpty()) {
            return Result.rejected(Rejection.NOTHING_TO_MIX);
        }

        boolean addsNewEffect = false;
        boolean additionHasEffects = false;
        for (StatusEffectInstance effect : addition) {
            additionHasEffects = true;
            addsNewEffect |= !merged.containsKey(effect.getEffectType());
            merged.merge(effect.getEffectType(), new StatusEffectInstance(effect), PotionMixing::stronger);
        }
        if (!additionHasEffects) {
            return Result.rejected(Rejection.NOTHING_TO_MIX);
        }
        if (!addsNewEffect) {
            return Result.rejected(Rejection.NO_NEW_EFFECT);
        }
        if (merged.size() > maxEffects) {
            return Result.rejected(Rejection.TOO_MANY_EFFECTS);
        }
        DurationContributions durations = baseDurations.plus(additionDurations);
        return Result.success(durations.applyTo(new ArrayList<>(merged.values())), durations);
    }

    /**
     * The stronger of two instances of the same effect: higher amplifier first, then longer duration. The winner's
     * duration is replaced by the average afterwards; this rule decides the amplifier and the display flags.
     */
    static StatusEffectInstance stronger(StatusEffectInstance a, StatusEffectInstance b) {
        if (a.getAmplifier() != b.getAmplifier()) {
            return a.getAmplifier() > b.getAmplifier() ? a : b;
        }
        if (a.isInfinite() || b.isInfinite()) {
            return a.isInfinite() ? a : b;
        }
        return a.getDuration() >= b.getDuration() ? a : b;
    }

    /**
     * The potion an empty bottle takes out of a cauldron: the unchanged potion if only one was poured in
     * (it still has its base potion), otherwise a mixed potion.
     */
    public static ItemStack bottle(Item kind, PotionContentsComponent contents, DurationContributions durations) {
        if (!contents.hasEffects()) {
            return PotionContentsComponent.createStack(kind, Potions.WATER);
        }
        if (contents.potion().isPresent()) {
            ItemStack stack = new ItemStack(kind);
            stack.set(DataComponentTypes.POTION_CONTENTS, contents);
            return stack;
        }
        return createMixedPotion(kind, contents.customEffects(), durations);
    }

    /**
     * Builds a mixed potion: explicit effects and no base potion, so the color is blended from the effects.
     *
     * <p>Without a base potion vanilla would call it "Uncraftable Potion", so the stack gets an item name
     * ({@link DataComponentTypes#ITEM_NAME}, not a custom name: it is not italic and anvils treat it as the
     * default name). The English fallback keeps the name readable on clients without the language file.
     * The duration contributions are stored on the stack, so the potion can be mixed again exactly.
     */
    public static ItemStack createMixedPotion(Item kind, List<StatusEffectInstance> effects, DurationContributions durations) {
        ItemStack stack = new ItemStack(kind);
        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.empty(), List.copyOf(effects)));
        stack.set(DataComponentTypes.ITEM_NAME, mixedPotionName(kind));
        durations.writeTo(stack);
        return stack;
    }

    public static Text mixedPotionName(Item kind) {
        if (kind == Items.SPLASH_POTION) {
            return Text.translatableWithFallback("item.progressionrebalance.mixed_splash_potion", "Mixed Splash Potion");
        }
        if (kind == Items.LINGERING_POTION) {
            return Text.translatableWithFallback("item.progressionrebalance.mixed_lingering_potion", "Mixed Lingering Potion");
        }
        return Text.translatableWithFallback("item.progressionrebalance.mixed_potion", "Mixed Potion");
    }

    public static Iterable<StatusEffectInstance> effectsOf(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getEffects();
    }
}
