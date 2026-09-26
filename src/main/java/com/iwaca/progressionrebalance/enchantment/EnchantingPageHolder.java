package com.iwaca.progressionrebalance.enchantment;

/**
 * Implemented by {@link net.minecraft.screen.EnchantmentScreenHandler} through a mixin. The page is a synced
 * screen handler property, so the client always displays the costs of the page the server is using.
 */
public interface EnchantingPageHolder {
    int progressionrebalance$getPage();
}
