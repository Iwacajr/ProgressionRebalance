package com.iwaca.progressionrebalance.enchantment;

import com.iwaca.progressionrebalance.mixin.LivingEntityAccessor;
import java.util.function.BooleanSupplier;
import net.minecraft.entity.LivingEntity;

/**
 * Deals damage outside a living entity's damage cooldown ("i-frames").
 *
 * <p>In {@code LivingEntity.damage}, a hit landing while {@code timeUntilRegen > 10} only deals the part above
 * {@code lastDamageTaken}; any other hit sets {@code timeUntilRegen = 20} and {@code lastDamageTaken}. Thorns
 * damage would therefore either be cut by an earlier hit or start a cooldown that cuts the next real attack.
 * {@link #runOutside} clears the cooldown for the one call and then restores both fields exactly, so the
 * damage is dealt in full and every other attack sees the cooldown as if the damage never happened.
 * The damage itself (health, hurt animation, death, attacker tracking) is completely vanilla.
 */
public final class HurtCooldown {
    private HurtCooldown() {
    }

    public static boolean runOutside(LivingEntity entity, BooleanSupplier damage) {
        LivingEntityAccessor accessor = (LivingEntityAccessor) entity;
        int cooldown = entity.timeUntilRegen;
        float lastDamageTaken = accessor.progressionrebalance$getLastDamageTaken();
        entity.timeUntilRegen = 0;
        try {
            return damage.getAsBoolean();
        } finally {
            entity.timeUntilRegen = cooldown;
            accessor.progressionrebalance$setLastDamageTaken(lastDamageTaken);
        }
    }
}
