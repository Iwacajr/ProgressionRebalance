package com.iwaca.progressionrebalance.client;

import com.iwaca.progressionrebalance.potion.PotionCauldrons;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.component.type.PotionContentsComponent;

public class ProgressionRebalanceClient implements ClientModInitializer {
    private static final int DEFAULT_COLOR = PotionContentsComponent.DEFAULT.getColor();

    @Override
    public void onInitializeClient() {
        // Tints the liquid of a potion cauldron (tint index 0 of the vanilla water cauldron models) with its mixture's color.
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                world != null && pos != null && world.getBlockEntityRenderData(pos) instanceof Integer color ? color : DEFAULT_COLOR,
                PotionCauldrons.BLOCK);
    }
}
