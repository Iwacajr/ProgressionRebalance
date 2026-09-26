package com.iwaca.progressionrebalance.client.datagen;

import com.iwaca.progressionrebalance.enchantment.RebalanceTags;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EntityTypeTags;

public class EntityTypeTagProvider extends FabricTagProvider.EntityTypeTagProvider {
    public EntityTypeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        // Bane targets: the vanilla arthropods plus other monster-like, non-humanoid enemies.
        getOrCreateTagBuilder(RebalanceTags.BANE_TARGETS)
                .add(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.ENDERMITE)
                .add(EntityType.CREEPER, EntityType.SLIME, EntityType.MAGMA_CUBE)
                .add(EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.RAVAGER);
        // The Bane of Arthropods enchantment checks this vanilla tag (damage bonus and Slowness). Adding to it,
        // rather than replacing it, keeps vanilla's own members (#minecraft:arthropod, including bees).
        getOrCreateTagBuilder(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS).addTag(RebalanceTags.BANE_TARGETS);
    }
}
