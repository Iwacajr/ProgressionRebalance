package com.iwaca.progressionrebalance.mixin;

import java.util.List;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets {@link PotionsMixin} replace a vanilla potion's effect list before it is registered. */
@Mixin(Potion.class)
public interface PotionAccessor {
    @Mutable
    @Accessor("effects")
    void progressionrebalance$setEffects(List<StatusEffectInstance> effects);
}
