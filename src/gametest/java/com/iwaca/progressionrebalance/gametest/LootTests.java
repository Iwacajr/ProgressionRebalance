package com.iwaca.progressionrebalance.gametest;

import com.iwaca.progressionrebalance.potion.ModPotions;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.Vec3d;

public class LootTests implements FabricGameTest {
    private static final String LUCK_PROVIDER = "progressionrebalance:luck_bonus_rolls";

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void structuresContainThematicPotions(TestContext context) {
        expectSome(context, LootTables.NETHER_BRIDGE_CHEST, 400, isPotion(Potions.FIRE_RESISTANCE), "fire resistance in fortresses");
        expectSome(context, LootTables.SHIPWRECK_SUPPLY_CHEST, 400, isPotion(Potions.WATER_BREATHING), "water breathing in shipwrecks");
        expectSome(context, LootTables.END_CITY_TREASURE_CHEST, 1500, isPotion(Potions.LUCK), "luck in end cities");
        expectSome(context, LootTables.ANCIENT_CITY_CHEST, 1500, isPotion(ModPotions.RESISTANCE), "resistance in ancient cities");
        expectSome(context, LootTables.ABANDONED_MINESHAFT_CHEST, 400, stack -> stack.isOf(Items.MINECART), "minecarts in mineshafts");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void potionsAreNotGuaranteed(TestContext context) {
        int samples = 1000;
        int withPotion = 0;
        LootTable table = table(context, LootTables.NETHER_BRIDGE_CHEST);
        for (int i = 0; i < samples; i++) {
            if (generate(context, table, 0.0F, i).stream().anyMatch(stack -> stack.isOf(Items.POTION))) {
                withPotion++;
            }
        }
        double share = withPotion / (double) samples;
        context.assertTrue(share > 0.3 && share < 0.65, "share of fortress chests with a potion: " + share);
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void luckAddsLootWithoutGuaranteeingIt(TestContext context) {
        LootTable table = table(context, LootTables.ABANDONED_MINESHAFT_CHEST);
        int samples = 600;
        double unlucky = averageStacks(context, table, 0.0F, samples);
        double negative = averageStacks(context, table, -1.0F, samples);
        double luckOne = averageStacks(context, table, 1.0F, samples);
        double luckThree = averageStacks(context, table, 3.0F, samples);
        context.assertTrue(unlucky == negative, "negative luck adds nothing: " + unlucky + " vs " + negative);
        context.assertTrue(luckOne > unlucky * 1.1, "Luck I noticeably increases loot: " + unlucky + " -> " + luckOne);
        context.assertTrue(luckThree > luckOne, "more luck gives more loot: " + luckOne + " -> " + luckThree);
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void luckTargetsOnlyRareMobDrops(TestContext context) {
        context.assertTrue(json(context, EntityType.ZOMBIE.getLootTableId()).contains(LUCK_PROVIDER), "zombie rare drops are luck sensitive");
        context.assertTrue(json(context, EntityType.WITHER_SKELETON.getLootTableId()).contains(LUCK_PROVIDER), "wither skull is luck sensitive");
        context.assertFalse(json(context, EntityType.BLAZE.getLootTableId()).contains(LUCK_PROVIDER), "common drops are unchanged");
        context.assertTrue(json(context, EntityType.WITCH.getLootTableId()).contains("minecraft:potion"), "witches can drop brewed potions");
        context.assertFalse(json(context, LootTables.SPAWN_BONUS_CHEST).contains(LUCK_PROVIDER), "bonus chest is excluded");
        context.assertFalse(json(context, LootTables.TRIAL_CHAMBERS_REWARD_CHEST).contains(LUCK_PROVIDER), "trial vaults are excluded");
        context.complete();
    }

    private static Predicate<ItemStack> isPotion(RegistryEntry<Potion> potion) {
        return stack -> stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).matches(potion);
    }

    private static void expectSome(TestContext context, RegistryKey<LootTable> key, int samples, Predicate<ItemStack> predicate, String what) {
        LootTable table = table(context, key);
        for (int i = 0; i < samples; i++) {
            if (generate(context, table, 0.0F, i).stream().anyMatch(predicate)) {
                return;
            }
        }
        context.throwGameTestException("never generated " + what + " in " + samples + " samples");
    }

    private static double averageStacks(TestContext context, LootTable table, float luck, int samples) {
        long total = 0;
        for (int i = 0; i < samples; i++) {
            total += generate(context, table, luck, i).size();
        }
        return total / (double) samples;
    }

    private static LootTable table(TestContext context, RegistryKey<LootTable> key) {
        return context.getWorld().getServer().getReloadableRegistries().getLootTable(key);
    }

    /** Seeds start at 1: vanilla treats seed 0 as "use the world random", which would make samples irreproducible. */
    private static ObjectArrayList<ItemStack> generate(TestContext context, LootTable table, float luck, int sample) {
        long seed = sample + 1L;
        LootContextParameterSet parameters = new LootContextParameterSet.Builder(context.getWorld())
                .add(LootContextParameters.ORIGIN, Vec3d.ZERO)
                .luck(luck)
                .build(LootContextTypes.CHEST);
        return table.generateLoot(parameters, seed);
    }

    private static String json(TestContext context, RegistryKey<LootTable> key) {
        return LootTable.CODEC.encodeStart(context.getWorld().getRegistryManager().getOps(JsonOps.INSTANCE), table(context, key))
                .getOrThrow().toString();
    }
}
