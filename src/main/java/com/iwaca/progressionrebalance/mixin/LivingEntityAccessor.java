package com.iwaca.progressionrebalance.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Why: the damage cooldown state saved and restored around Thorns damage (see {@code HurtCooldown}) is protected. */
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("lastDamageTaken")
    float progressionrebalance$getLastDamageTaken();

    @Accessor("lastDamageTaken")
    void progressionrebalance$setLastDamageTaken(float lastDamageTaken);
}
