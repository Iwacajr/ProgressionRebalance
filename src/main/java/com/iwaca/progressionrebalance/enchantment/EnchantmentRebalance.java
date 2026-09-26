package com.iwaca.progressionrebalance.enchantment;

import com.iwaca.progressionrebalance.config.ConfigManager;
import com.iwaca.progressionrebalance.config.ModConfig;
import com.iwaca.progressionrebalance.mixin.EnchantmentBuilderAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.effect.AllOfEnchantmentEffects;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.enchantment.effect.TargetedEnchantmentEffect;
import net.minecraft.enchantment.effect.entity.DamageEntityEnchantmentEffect;
import net.minecraft.enchantment.effect.value.AddEnchantmentEffect;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.loot.condition.AllOfLootCondition;
import net.minecraft.loot.condition.InvertedLootCondition;
import net.minecraft.loot.condition.LootCondition;

/**
 * Rebalances vanilla enchantments by editing their data-driven definitions while they load (Fabric's
 * {@link EnchantmentEvents#MODIFY}), instead of patching damage code. Only builtin definitions (vanilla or
 * mod-bundled) are touched; an enchantment replaced by a data pack is left exactly as the pack defines it.
 *
 * <ul>
 *     <li><b>Protection</b>: its damage protection is scaled from 4% to {@code protectionReductionPerLevel}%
 *     per level. Vanilla adds up all protection points, caps them at 20 (80%) and gives 4% per point, so the
 *     specialized protections and the cap are unaffected.</li>
 *     <li><b>Thorns</b>: its thorns damage range is scaled by {@code thornsDamageMultiplier}. The damage is
 *     rolled uniformly in that range, so every hit is scaled. Its cooldown handling is in
 *     {@code DamageEntityEnchantmentEffectMixin}.</li>
 *     <li><b>Impaling</b>: every conditional damage bonus gets a companion bonus, scaled by
 *     {@code impalingWetMultiplier}, for targets that fail the original condition (not aquatic) but are
 *     {@linkplain WetLootCondition wet}. The companion requires the original condition to be false, so aquatic
 *     targets never get both.</li>
 * </ul>
 * Bane needs no code: its target list is the entity tag {@link RebalanceTags#BANE_TARGETS}.
 *
 * <p>Enchantment effects are only evaluated on the server, so these edits affect gameplay only there.
 */
public final class EnchantmentRebalance {
    /** Vanilla: one protection point (4% damage reduction) per level of Protection. */
    static final double VANILLA_PROTECTION_PERCENT_PER_LEVEL = 4.0;

    private EnchantmentRebalance() {
    }

    public static void register() {
        EnchantmentEvents.MODIFY.register((key, builder, source) -> {
            if (!source.isBuiltin()) {
                return;
            }
            ModConfig.Enchantments config = ConfigManager.get().enchantments();
            if (key.equals(Enchantments.PROTECTION)) {
                scaleProtection(effects(builder, EnchantmentEffectComponentTypes.DAMAGE_PROTECTION), protectionFactor(config.protectionReductionPerLevel()));
            } else if (key.equals(Enchantments.THORNS)) {
                scaleThorns(effects(builder, EnchantmentEffectComponentTypes.POST_ATTACK), (float) config.thornsDamageMultiplier());
            } else if (key.equals(Enchantments.IMPALING) && config.impalingWetMultiplier() > 0.0) {
                addWetBonus(effects(builder, EnchantmentEffectComponentTypes.DAMAGE), (float) config.impalingWetMultiplier());
            }
        });
    }

    /** How much of vanilla's Protection value remains: 0.75 for the default 3% per level. */
    public static float protectionFactor(double percentPerLevel) {
        return (float) (percentPerLevel / VANILLA_PROTECTION_PERCENT_PER_LEVEL);
    }

    static void scaleProtection(List<EnchantmentEffectEntry<EnchantmentValueEffect>> protection, float factor) {
        protection.replaceAll(entry -> entry.effect() instanceof AddEnchantmentEffect add
                ? new EnchantmentEffectEntry<>(new AddEnchantmentEffect(EnchantmentValues.scale(add.value(), factor)), entry.requirements())
                : entry);
    }

    static void scaleThorns(List<TargetedEnchantmentEffect<EnchantmentEntityEffect>> postAttack, float factor) {
        postAttack.replaceAll(entry -> new TargetedEnchantmentEffect<>(
                entry.enchanted(), entry.affected(), scaleThornsDamage(entry.effect(), factor), entry.requirements()));
    }

    private static EnchantmentEntityEffect scaleThornsDamage(EnchantmentEntityEffect effect, float factor) {
        if (effect instanceof DamageEntityEnchantmentEffect damage && damage.damageType().matchesKey(DamageTypes.THORNS)) {
            return new DamageEntityEnchantmentEffect(
                    EnchantmentValues.scale(damage.minDamage(), factor), EnchantmentValues.scale(damage.maxDamage(), factor), damage.damageType());
        }
        if (effect instanceof AllOfEnchantmentEffects.EntityEffects allOf) {
            return new AllOfEnchantmentEffects.EntityEffects(allOf.effects().stream().map(inner -> scaleThornsDamage(inner, factor)).toList());
        }
        return effect;
    }

    static void addWetBonus(List<EnchantmentEffectEntry<EnchantmentValueEffect>> damage, float factor) {
        List<EnchantmentEffectEntry<EnchantmentValueEffect>> wetBonuses = new ArrayList<>();
        for (EnchantmentEffectEntry<EnchantmentValueEffect> entry : damage) {
            if (entry.effect() instanceof AddEnchantmentEffect add && entry.requirements().isPresent()) {
                LootCondition wetInstead = AllOfLootCondition.create(List.of(
                        new InvertedLootCondition(entry.requirements().get()), WetLootCondition.INSTANCE));
                wetBonuses.add(new EnchantmentEffectEntry<>(new AddEnchantmentEffect(EnchantmentValues.scale(add.value(), factor)), Optional.of(wetInstead)));
            }
        }
        damage.addAll(wetBonuses);
    }

    /** The builder's mutable effect list; Fabric pre-fills it with the enchantment's current effects. */
    private static <E> List<E> effects(Enchantment.Builder builder, ComponentType<List<E>> type) {
        return ((EnchantmentBuilderAccessor) builder).progressionrebalance$getEffectsList(type);
    }
}
