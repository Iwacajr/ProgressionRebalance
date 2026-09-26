package com.iwaca.progressionrebalance.mixin;

import com.google.gson.JsonElement;
import com.iwaca.progressionrebalance.config.ConfigManager;
import com.iwaca.progressionrebalance.recipe.RailRecipeOutputs;
import java.util.Map;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Why: rail recipe output counts are configurable, but recipes are plain data. Adjusting the parsed
 * result right after a (re)load keeps the JSON untouched (no vanilla recipe file is overridden) while
 * still reaching the recipe book, because recipes are synced to clients after this point.
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Shadow
    private Map<Identifier, RecipeEntry<?>> recipesById;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("TAIL"))
    private void progressionrebalance$applyRailOutputs(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
        RailRecipeOutputs.apply(this.recipesById, ConfigManager.get().rails());
    }
}
