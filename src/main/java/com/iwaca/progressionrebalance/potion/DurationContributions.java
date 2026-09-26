package com.iwaca.progressionrebalance.potion;

import com.iwaca.progressionrebalance.ProgressionRebalance;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

/**
 * Where the timed durations of a mixture come from: the sum of the original durations of every potion poured
 * into it, and how many potions that was. Every timed effect of a mixture lasts the average,
 * {@code totalTicks / contributors}.
 *
 * <p>Keeping the sum and the count, rather than only the current average, makes chained mixing exact: pouring a
 * 3:00 potion into a mixture of 16:00 and 12:00 gives (16 + 12 + 3) / 3 = 10:20, not (14 + 3) / 2 = 8:30.
 *
 * <p>An ordinary potion is one contributor with the duration of its timed effects. Instant effects (Instant
 * Health, Instant Damage) and infinite effects have no duration to average: they contribute nothing and are
 * left unchanged. A potion with only such effects contributes nothing at all.
 *
 * <p>Mixed potions carry their contributions in the vanilla {@code minecraft:custom_data} component, so the data
 * is saved, synced and moved with the stack like any other item data, and a world that later removes the mod
 * keeps its potions. Potion cauldrons store them in their block entity.
 */
public record DurationContributions(long totalTicks, int contributors) {
    public static final DurationContributions NONE = new DurationContributions(0L, 0);

    /** Key inside {@code custom_data} of a mixed potion. */
    static final String CUSTOM_DATA_KEY = ProgressionRebalance.MOD_ID + ":mixed_durations";
    private static final String TOTAL_TICKS_KEY = "total_ticks";
    private static final String CONTRIBUTORS_KEY = "contributors";

    public DurationContributions {
        if (totalTicks < 0 || contributors < 0 || (contributors == 0) != (totalTicks == 0)) {
            throw new IllegalArgumentException("invalid duration contributions: " + totalTicks + " ticks from " + contributors);
        }
    }

    /** Whether an effect's duration takes part in averaging: not instant and not infinite. */
    public static boolean isTimed(StatusEffectInstance effect) {
        return !effect.getEffectType().value().isInstant() && !effect.isInfinite();
    }

    /**
     * What one ordinary potion contributes: one contributor with the duration of its timed effects. Vanilla
     * potions give all their effects the same duration; if they differ, their average is used.
     */
    public static DurationContributions ofPotionEffects(Iterable<StatusEffectInstance> effects) {
        long total = 0L;
        int timed = 0;
        for (StatusEffectInstance effect : effects) {
            if (isTimed(effect)) {
                total += effect.getDuration();
                timed++;
            }
        }
        return timed == 0 ? NONE : new DurationContributions(Math.max(1L, Math.round((double) total / timed)), 1);
    }

    /** What a potion stack contributes: the stored contributions of a mixed potion, otherwise its own duration. */
    public static DurationContributions of(ItemStack stack) {
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data != null && data.contains(CUSTOM_DATA_KEY)) {
            Optional<DurationContributions> stored = fromNbt(data.copyNbt().getCompound(CUSTOM_DATA_KEY));
            if (stored.isPresent()) {
                return stored.get();
            }
        }
        return ofPotionEffects(PotionMixing.effectsOf(stack));
    }

    public DurationContributions plus(DurationContributions other) {
        return new DurationContributions(totalTicks + other.totalTicks, contributors + other.contributors);
    }

    public boolean isEmpty() {
        return contributors == 0;
    }

    /** The duration every timed effect gets, rounded to the nearest tick. */
    public int averageTicks() {
        if (isEmpty()) {
            throw new IllegalStateException("no durations to average");
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1L, Math.round((double) totalTicks / contributors)));
    }

    /**
     * Gives every timed effect the average duration. Amplifiers, particle and icon flags are kept; instant and
     * infinite effects are returned unchanged.
     */
    public List<StatusEffectInstance> applyTo(List<StatusEffectInstance> effects) {
        if (isEmpty()) {
            return List.copyOf(effects);
        }
        int duration = averageTicks();
        List<StatusEffectInstance> result = new ArrayList<>(effects.size());
        for (StatusEffectInstance effect : effects) {
            result.add(isTimed(effect)
                    ? new StatusEffectInstance(effect.getEffectType(), duration, effect.getAmplifier(),
                            effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon())
                    : new StatusEffectInstance(effect));
        }
        return List.copyOf(result);
    }

    /** Stores these contributions on a mixed potion. Nothing is stored when there are none. */
    public void writeTo(ItemStack stack) {
        if (!isEmpty()) {
            NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.put(CUSTOM_DATA_KEY, toNbt()));
        }
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putLong(TOTAL_TICKS_KEY, totalTicks);
        nbt.putInt(CONTRIBUTORS_KEY, contributors);
        return nbt;
    }

    /** Reads stored contributions; empty if they are missing or invalid (e.g. edited by hand). */
    public static Optional<DurationContributions> fromNbt(NbtCompound nbt) {
        if (!nbt.contains(TOTAL_TICKS_KEY, NbtElement.NUMBER_TYPE) || !nbt.contains(CONTRIBUTORS_KEY, NbtElement.NUMBER_TYPE)) {
            return Optional.empty();
        }
        long total = nbt.getLong(TOTAL_TICKS_KEY);
        int contributors = nbt.getInt(CONTRIBUTORS_KEY);
        if (total <= 0 || contributors <= 0) {
            return Optional.empty();
        }
        return Optional.of(new DurationContributions(total, contributors));
    }
}
