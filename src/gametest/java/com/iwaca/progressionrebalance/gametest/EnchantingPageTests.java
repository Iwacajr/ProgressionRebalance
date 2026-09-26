package com.iwaca.progressionrebalance.gametest;

import com.iwaca.progressionrebalance.enchantment.EnchantingPageHolder;
import java.util.Arrays;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class EnchantingPageTests implements FabricGameTest {
    private static final BlockPos TABLE = new BlockPos(3, 1, 3);

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void lapisSelectsDeterministicPages(TestContext context) {
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        EnchantmentScreenHandler handler = openTable(context, player);
        handler.getSlot(0).setStack(new ItemStack(Items.DIAMOND_PICKAXE));

        String[] pages = new String[3];
        for (int page = 0; page < 3; page++) {
            for (int lapis = page * 3 + 1; lapis <= page * 3 + 3; lapis++) {
                setLapis(handler, lapis);
                context.assertTrue(page(handler) == page, lapis + " lapis should show page " + page + " but showed " + page(handler));
                String offers = offers(handler);
                if (pages[page] == null) {
                    pages[page] = offers;
                }
                context.assertTrue(pages[page].equals(offers), "offers must not change within page " + page);
            }
        }
        setLapis(handler, 64);
        context.assertTrue(page(handler) == 2 && offers(handler).equals(pages[2]), "extra lapis stays on the last page");
        context.assertFalse(pages[0].equals(pages[1]) && pages[1].equals(pages[2]), "pages should offer different enchantments");

        setLapis(handler, 5);
        setLapis(handler, 1);
        setLapis(handler, 5);
        context.assertTrue(offers(handler).equals(pages[1]), "returning to a page shows the same offers (deterministic)");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void pageCostsAreChargedAndSeedAdvances(TestContext context) {
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        player.experienceLevel = 100;
        EnchantmentScreenHandler handler = openTable(context, player);
        handler.getSlot(0).setStack(new ItemStack(Items.BOOK));
        setLapis(handler, 5);
        context.assertTrue(page(handler) == 1, "5 lapis is page 2");

        context.assertFalse(handler.onButtonClick(player, 2), "third offer on page 2 costs 6 lapis");
        context.assertTrue(handler.getSlot(1).getStack().getCount() == 5, "failed click consumes nothing");

        int seedBefore = player.getEnchantmentTableSeed();
        context.assertTrue(handler.onButtonClick(player, 0), "first offer on page 2 costs 4 lapis");
        context.assertTrue(handler.getSlot(1).getStack().getCount() == 1, "4 lapis consumed");
        context.assertTrue(handler.getSlot(0).getStack().isOf(Items.ENCHANTED_BOOK), "book was enchanted");
        context.assertTrue(player.experienceLevel == 99, "experience cost stays vanilla (1 level for the first offer)");
        context.assertTrue(player.getEnchantmentTableSeed() != seedBefore, "enchanting rerolls the player's seed");
        context.assertTrue(page(handler) == 0, "remaining lapis moves back to page 1");

        handler.getSlot(0).setStack(new ItemStack(Items.BOOK));
        setLapis(handler, 3);
        context.assertTrue(handler.onButtonClick(player, 2), "page 1 keeps vanilla costs");
        context.assertTrue(handler.getSlot(1).getStack().isEmpty(), "3 lapis consumed");
        context.complete();
    }

    private static EnchantmentScreenHandler openTable(TestContext context, PlayerEntity player) {
        context.setBlockState(TABLE, Blocks.ENCHANTING_TABLE);
        for (BlockPos offset : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
            context.setBlockState(TABLE.add(offset), Blocks.BOOKSHELF);
        }
        return new EnchantmentScreenHandler(0, player.getInventory(),
                ScreenHandlerContext.create(context.getWorld(), context.getAbsolutePos(TABLE)));
    }

    private static void setLapis(EnchantmentScreenHandler handler, int count) {
        handler.getSlot(1).setStack(new ItemStack(Items.LAPIS_LAZULI, count));
    }

    private static int page(EnchantmentScreenHandler handler) {
        return ((EnchantingPageHolder) handler).progressionrebalance$getPage();
    }

    private static String offers(EnchantmentScreenHandler handler) {
        return Arrays.toString(handler.enchantmentPower) + Arrays.toString(handler.enchantmentId) + Arrays.toString(handler.enchantmentLevel);
    }
}
