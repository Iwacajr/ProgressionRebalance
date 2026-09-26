package com.iwaca.progressionrebalance.mixin;

import com.iwaca.progressionrebalance.config.ConfigManager;
import com.iwaca.progressionrebalance.enchantment.EnchantingPageHolder;
import com.iwaca.progressionrebalance.enchantment.EnchantingPages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Why: lapis pages (see {@link EnchantingPages}) change which offers the table generates and how much lapis
 * each offer consumes. Everything happens in the screen handler, which is server-authoritative; the vanilla
 * screen and its three buttons are kept.
 *
 * <ul>
 *     <li>A synced {@link Property} holds the current page. The server recomputes it whenever the table's
 *     inventory changes, right before vanilla regenerates the offers.</li>
 *     <li>The two {@code Random.setSeed} calls used to generate offers (in the {@code onContentChanged} lambda,
 *     {@code method_17411}, and in {@code generateEnchantments}) get a page-specific seed, so preview and
 *     result always agree. Page 0 keeps the vanilla seed.</li>
 *     <li>{@code onButtonClick} requires the page's lapis cost, and the lapis consumed in the click lambda
 *     ({@code method_17410}) is that cost. Experience levels consumed stay vanilla (1/2/3).</li>
 * </ul>
 * The player's enchanting seed is still rerolled by vanilla after every enchant, so pages refresh too.
 */
@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin extends ScreenHandler implements EnchantingPageHolder {
    @Shadow
    @Final
    private Inventory inventory;

    @Shadow
    @Final
    private ScreenHandlerContext context;

    @Unique
    private final Property progressionrebalance$page = Property.create();

    protected EnchantmentScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Override
    public int progressionrebalance$getPage() {
        return this.progressionrebalance$page.get();
    }

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("TAIL"))
    private void progressionrebalance$trackPage(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        this.addProperty(this.progressionrebalance$page);
    }

    @Inject(method = "onContentChanged", at = @At("HEAD"))
    private void progressionrebalance$updatePage(Inventory changed, CallbackInfo ci) {
        if (changed == this.inventory) {
            // Only runs on the server (the client context is empty); the client receives the value via sync.
            this.context.run((world, pos) -> this.progressionrebalance$page.set(EnchantingPages.pageFor(
                    this.inventory.getStack(1).getCount(), ConfigManager.get().enchanting().effectiveMaxPages())));
        }
    }

    @ModifyArg(method = {"method_17411", "generateEnchantments"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/random/Random;setSeed(J)V"))
    private long progressionrebalance$pageSeed(long seed) {
        return EnchantingPages.pageSeed(seed, this.progressionrebalance$page.get());
    }

    @Inject(method = "onButtonClick", at = @At("HEAD"), cancellable = true)
    private void progressionrebalance$requirePageLapis(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (id < 0 || id >= EnchantingPages.OFFERS_PER_PAGE || player.isInCreativeMode()) {
            return;
        }
        if (this.inventory.getStack(1).getCount() < EnchantingPages.lapisCost(this.progressionrebalance$page.get(), id)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyArg(method = "method_17410", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrementUnlessCreative(ILnet/minecraft/entity/LivingEntity;)V"), index = 0)
    private int progressionrebalance$consumePageLapis(int vanillaCost) {
        // Vanilla consumes (slot + 1) lapis.
        return EnchantingPages.lapisCost(this.progressionrebalance$page.get(), vanillaCost - 1);
    }
}
