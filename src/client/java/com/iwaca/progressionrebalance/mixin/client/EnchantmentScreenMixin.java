package com.iwaca.progressionrebalance.mixin.client;

import com.iwaca.progressionrebalance.enchantment.EnchantingPageHolder;
import com.iwaca.progressionrebalance.enchantment.EnchantingPages;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Why: the vanilla screen assumes offer {@code n} costs {@code n + 1} lapis, both when greying out buttons
 * and in the tooltip. With lapis pages the cost is {@code 3 * page + n + 1}. The screen layout is unchanged;
 * only those numbers are corrected, using the page synced from the server-side screen handler.
 */
@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends HandledScreen<EnchantmentScreenHandler> {
    @Unique
    private static final String LAPIS_ONE = "container.enchant.lapis.one";
    @Unique
    private static final String LAPIS_MANY = "container.enchant.lapis.many";

    protected EnchantmentScreenMixin(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Unique
    private int progressionrebalance$page() {
        return ((EnchantingPageHolder) this.handler).progressionrebalance$getPage();
    }

    /** Vanilla compares the lapis count against {@code slot + 1}; compare lapis above the page's base instead. */
    @ModifyExpressionValue(method = {"drawBackground", "render"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/EnchantmentScreenHandler;getLapisCount()I"))
    private int progressionrebalance$lapisAbovePage(int lapisCount) {
        return lapisCount - progressionrebalance$page() * EnchantingPages.OFFERS_PER_PAGE;
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;"))
    private MutableText progressionrebalance$singleLapisCost(String key, Operation<MutableText> original) {
        int page = progressionrebalance$page();
        if (LAPIS_ONE.equals(key) && page > 0) {
            return Text.translatable(LAPIS_MANY, EnchantingPages.lapisCost(page, 0));
        }
        return original.call(key);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"))
    private MutableText progressionrebalance$pageLapisCost(String key, Object[] args, Operation<MutableText> original) {
        if (LAPIS_MANY.equals(key) && args.length == 1 && args[0] instanceof Integer vanillaCost) {
            return original.call(key, new Object[]{EnchantingPages.lapisCost(progressionrebalance$page(), vanillaCost - 1)});
        }
        return original.call(key, args);
    }

    /** Adds a single "Offer page N" line to the offer tooltip once the player is beyond the first page. */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;II)V"))
    private void progressionrebalance$showPage(DrawContext context, TextRenderer textRenderer, List<Text> lines, int x, int y, Operation<Void> original) {
        int page = progressionrebalance$page();
        if (page > 0) {
            lines.add(Text.translatable("container.progressionrebalance.enchant.page", page + 1).formatted(Formatting.DARK_GRAY));
        }
        original.call(context, textRenderer, lines, x, y);
    }
}
