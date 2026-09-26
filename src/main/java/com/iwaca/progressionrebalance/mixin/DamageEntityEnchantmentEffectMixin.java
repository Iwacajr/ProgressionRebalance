package com.iwaca.progressionrebalance.mixin;

import com.iwaca.progressionrebalance.enchantment.HurtCooldown;
import com.iwaca.progressionrebalance.enchantment.RebalanceTags;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.enchantment.effect.entity.DamageEntityEnchantmentEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Why: Thorns deals its damage through this enchantment effect, as an ordinary {@code damage} call, so it
 * starts the attacker's damage cooldown and the player's next sword hit is partly or fully absorbed.
 * Only this call site is wrapped, and only for damage types in {@code #progressionrebalance:ignores_hurt_cooldown}
 * (Thorns by default): the damage is dealt outside the cooldown ({@link HurtCooldown}). Other damage, including
 * Guardian spikes (which also use the thorns damage type but not this effect), keeps vanilla cooldowns.
 * Enchantment post-attack effects only run from attack code, never from {@code damage} itself, so this cannot
 * make Thorns trigger Thorns.
 */
@Mixin(DamageEntityEnchantmentEffect.class)
public abstract class DamageEntityEnchantmentEffectMixin {
    @WrapOperation(method = "apply", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean progressionrebalance$outsideHurtCooldown(Entity target, DamageSource source, float amount, Operation<Boolean> original) {
        if (target instanceof LivingEntity living && source.isIn(RebalanceTags.IGNORES_HURT_COOLDOWN)) {
            return HurtCooldown.runOutside(living, () -> original.call(target, source, amount));
        }
        return original.call(target, source, amount);
    }
}
