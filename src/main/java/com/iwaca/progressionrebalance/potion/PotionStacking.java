package com.iwaca.progressionrebalance.potion;

import com.iwaca.progressionrebalance.config.ConfigManager;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Items;

/**
 * Optional limited stacking of drinkable potions.
 *
 * <p>This only changes the default {@code max_stack_size} component, which vanilla already handles for
 * potions: drinking from a stack returns the empty bottle to the inventory, brewing stand slots still hold
 * a single bottle, and potions with different contents (including mixed potions) never stack together.
 * Splash and lingering potions are intentionally left at 1 so throwables are not spammable in combat.
 */
public final class PotionStacking {
    private PotionStacking() {
    }

    public static void register() {
        int stackSize = ConfigManager.get().potions().drinkableStackSize();
        if (stackSize <= 1) {
            return;
        }
        DefaultItemComponentEvents.MODIFY.register(context ->
                context.modify(Items.POTION, builder -> builder.add(DataComponentTypes.MAX_STACK_SIZE, stackSize)));
    }
}
