package com.iwaca.progressionrebalance.enchantment;

import com.iwaca.progressionrebalance.ProgressionRebalance;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

/** Data-driven lists used by the enchantment rebalance. Data packs and mods can add to them. */
public final class RebalanceTags {
    /**
     * Entities that take the Bane bonus (the vanilla {@code bane_of_arthropods} enchantment). The vanilla tag
     * {@code #minecraft:sensitive_to_bane_of_arthropods}, which the enchantment checks, includes this tag.
     */
    public static final TagKey<EntityType<?>> BANE_TARGETS = TagKey.of(RegistryKeys.ENTITY_TYPE, ProgressionRebalance.id("bane_targets"));

    /**
     * Damage types that, when dealt by an enchantment effect (Thorns), neither are blocked by nor start the
     * target's damage cooldown. See {@link HurtCooldown}.
     */
    public static final TagKey<DamageType> IGNORES_HURT_COOLDOWN = TagKey.of(RegistryKeys.DAMAGE_TYPE, ProgressionRebalance.id("ignores_hurt_cooldown"));

    private RebalanceTags() {
    }
}
