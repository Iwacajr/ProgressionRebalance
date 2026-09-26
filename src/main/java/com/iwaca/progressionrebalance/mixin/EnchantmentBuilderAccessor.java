package com.iwaca.progressionrebalance.mixin;

import java.util.List;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Why: Fabric's enchantment MODIFY event hands out a vanilla {@link Enchantment.Builder}, which can only append
 * effects. Rescaling existing effects (Protection, Thorns) needs the builder's own mutable effect lists.
 */
@Mixin(Enchantment.Builder.class)
public interface EnchantmentBuilderAccessor {
    @Invoker("getEffectsList")
    <E> List<E> progressionrebalance$getEffectsList(ComponentType<List<E>> type);
}
