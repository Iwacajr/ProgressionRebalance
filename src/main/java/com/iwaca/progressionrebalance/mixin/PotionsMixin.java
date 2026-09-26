package com.iwaca.progressionrebalance.mixin;

import com.iwaca.progressionrebalance.config.ConfigManager;
import com.iwaca.progressionrebalance.potion.PotionDurationPolicy;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Why: brewed potion durations live in the {@link Potion} objects that vanilla builds in the static
 * initializer of {@link Potions}. Hooking the private {@code register} helper scales exactly the vanilla
 * potions (never potions added by other mods) once, before they enter the registry, so every consumer
 * (brewing, tooltips, splash/lingering/tipped arrow derivations, commands) sees consistent values.
 */
@Mixin(Potions.class)
public abstract class PotionsMixin {
    @Inject(method = "register", at = @At("HEAD"))
    private static void progressionrebalance$rebalanceDuration(String name, Potion potion, CallbackInfoReturnable<RegistryEntry<Potion>> cir) {
        ((PotionAccessor) potion).progressionrebalance$setEffects(
                PotionDurationPolicy.rebalance(potion.getEffects(), ConfigManager.get().potions()));
    }
}
