package com.iwaca.progressionrebalance.gametest;

import com.iwaca.progressionrebalance.recipe.RailRecipeOutputs;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class EquipmentAndRecipeTests implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void railRecipesUseConfiguredOutputs(TestContext context) {
        assertResult(context, RailRecipeOutputs.COPPER_RAIL, Items.RAIL, 8);
        assertResult(context, RailRecipeOutputs.IRON_RAIL, Items.RAIL, 16);
        assertResult(context, RailRecipeOutputs.POWERED_RAIL, Items.POWERED_RAIL, 12);
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void goldToolsAreDurableFastAndIronTier(TestContext context) {
        ItemStack pickaxe = new ItemStack(Items.GOLDEN_PICKAXE);
        context.assertTrue(pickaxe.getMaxDamage() == 128, "golden pickaxe durability " + pickaxe.getMaxDamage());
        context.assertTrue(new ItemStack(Items.GOLDEN_AXE).getMaxDamage() == 128, "golden axe keeps the tool durability");
        context.assertTrue(new ItemStack(Items.GOLDEN_SHOVEL).getMaxDamage() == 128, "golden shovel keeps the tool durability");
        context.assertTrue(pickaxe.isSuitableFor(Blocks.DIAMOND_ORE.getDefaultState()), "golden pickaxe harvests diamond ore");
        context.assertTrue(pickaxe.isSuitableFor(Blocks.IRON_ORE.getDefaultState()), "golden pickaxe harvests iron ore");
        context.assertFalse(pickaxe.isSuitableFor(Blocks.OBSIDIAN.getDefaultState()), "golden pickaxe must not harvest obsidian");
        context.assertTrue(pickaxe.getMiningSpeedMultiplier(Blocks.STONE.getDefaultState()) == 12.0F, "gold keeps its mining speed");
        context.assertTrue(new ItemStack(Items.GOLDEN_SHOVEL).isSuitableFor(Blocks.DIRT.getDefaultState()), "shovel still works");
        context.assertTrue(new ItemStack(Items.IRON_PICKAXE).getMaxDamage() == 250, "iron is untouched");
        context.assertTrue(Items.GOLDEN_PICKAXE.getEnchantability() == 22, "gold keeps its tool enchantability");
        // The pickaxe's combat stats are vanilla: 2 attack damage, 1.2 attack speed.
        assertStat(context, pickaxe, EntityAttributes.GENERIC_ATTACK_DAMAGE, PLAYER_DAMAGE, 2.0);
        assertStat(context, pickaxe, EntityAttributes.GENERIC_ATTACK_SPEED, PLAYER_SPEED, 1.2);
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void goldWeaponsAreFastButFragile(TestContext context) {
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        context.assertTrue(sword.getMaxDamage() == 180, "golden sword durability " + sword.getMaxDamage());
        assertStat(context, sword, EntityAttributes.GENERIC_ATTACK_DAMAGE, PLAYER_DAMAGE, 6.5);
        assertStat(context, sword, EntityAttributes.GENERIC_ATTACK_SPEED, PLAYER_SPEED, 1.8);
        context.assertTrue(Items.GOLDEN_SWORD.getEnchantability() == 22, "gold keeps its weapon enchantability");

        ItemStack iron = new ItemStack(Items.IRON_SWORD);
        ItemStack diamond = new ItemStack(Items.DIAMOND_SWORD);
        context.assertTrue(iron.getMaxDamage() == 250 && diamond.getMaxDamage() == 1561, "iron and diamond durability untouched");
        assertStat(context, iron, EntityAttributes.GENERIC_ATTACK_DAMAGE, PLAYER_DAMAGE, 6.0);
        assertStat(context, diamond, EntityAttributes.GENERIC_ATTACK_DAMAGE, PLAYER_DAMAGE, 7.0);
        assertStat(context, diamond, EntityAttributes.GENERIC_ATTACK_SPEED, PLAYER_SPEED, 1.6);

        ItemStack axe = new ItemStack(Items.GOLDEN_AXE);
        assertStat(context, axe, EntityAttributes.GENERIC_ATTACK_DAMAGE, PLAYER_DAMAGE, 8.0);
        assertStat(context, axe, EntityAttributes.GENERIC_ATTACK_SPEED, PLAYER_SPEED, 1.1);
        assertStat(context, new ItemStack(Items.IRON_AXE), EntityAttributes.GENERIC_ATTACK_DAMAGE, PLAYER_DAMAGE, 9.0);
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void goldArmorSitsBetweenIronAndDiamondWithoutToughness(TestContext context) {
        ArmorStandEntity gold = wearing(context, 1, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
        ArmorStandEntity iron = wearing(context, 3, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        ArmorStandEntity diamond = wearing(context, 5, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
        context.assertTrue(((ArmorItem) Items.GOLDEN_CHESTPLATE).getEnchantability() == 25, "gold keeps its armor enchantability");
        context.assertTrue(new ItemStack(Items.GOLDEN_CHESTPLATE).getMaxDamage() < new ItemStack(Items.IRON_CHESTPLATE).getMaxDamage(),
                "gold armor stays less durable than iron");

        // Equipment attributes are applied on the next entity tick.
        context.addInstantFinalTask(() -> {
            assertArmor(context, gold, 18, 0);
            assertArmor(context, iron, 15, 0);
            assertArmor(context, diamond, 20, 8);
            context.assertFalse(gold.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) > 0.0, "gold has no knockback resistance");

            // An ordinary hit: gold is between iron and diamond. A big hit: diamond's toughness pulls far ahead.
            float smallGold = damageTaken(context, gold, 5.0F);
            float smallIron = damageTaken(context, iron, 5.0F);
            float smallDiamond = damageTaken(context, diamond, 5.0F);
            float bigGold = damageTaken(context, gold, 20.0F);
            float bigDiamond = damageTaken(context, diamond, 20.0F);
            context.assertTrue(smallIron > smallGold && smallGold > smallDiamond,
                    "5 damage: iron " + smallIron + " > gold " + smallGold + " > diamond " + smallDiamond);
            context.assertTrue(bigGold / bigDiamond > smallGold / smallDiamond,
                    "diamond's advantage grows with the hit: 5 damage " + smallGold + "/" + smallDiamond + ", 20 damage " + bigGold + "/" + bigDiamond);
        });
    }

    private static final double PLAYER_DAMAGE = 1.0;
    private static final double PLAYER_SPEED = 4.0;

    /** The main-hand value a player's tooltip shows: the player's base value plus the stack's modifiers. */
    private static void assertStat(TestContext context, ItemStack stack, RegistryEntry<EntityAttribute> attribute, double base, double expected) {
        double[] value = {base};
        stack.applyAttributeModifiers(EquipmentSlot.MAINHAND, (entry, modifier) -> {
            if (entry.equals(attribute)) {
                value[0] += modifier.value();
            }
        });
        context.assertTrue(Math.abs(value[0] - expected) < 1.0e-6, stack + " " + attribute.getIdAsString() + " = " + value[0] + ", expected " + expected);
    }

    private static ArmorStandEntity wearing(TestContext context, int x, Item... armor) {
        ArmorStandEntity stand = context.spawnEntity(EntityType.ARMOR_STAND, new BlockPos(x, 1, 1));
        for (Item piece : armor) {
            stand.equipStack(((ArmorItem) piece).getSlotType(), new ItemStack(piece));
        }
        return stand;
    }

    private static void assertArmor(TestContext context, LivingEntity entity, int armor, int toughness) {
        context.assertTrue(entity.getArmor() == armor, "armor " + entity.getArmor() + ", expected " + armor);
        double actualToughness = entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        context.assertTrue(actualToughness == toughness, "toughness " + actualToughness + ", expected " + toughness);
    }

    private static float damageTaken(TestContext context, LivingEntity entity, float damage) {
        return DamageUtil.getDamageLeft(entity, damage, context.getWorld().getDamageSources().generic(),
                entity.getArmor(), (float) entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void goldArmorIsMoreDurable(TestContext context) {
        context.assertTrue(new ItemStack(Items.GOLDEN_HELMET).getMaxDamage() == 11 * 11, "helmet");
        context.assertTrue(new ItemStack(Items.GOLDEN_CHESTPLATE).getMaxDamage() == 16 * 11, "chestplate");
        context.assertTrue(new ItemStack(Items.GOLDEN_LEGGINGS).getMaxDamage() == 15 * 11, "leggings");
        context.assertTrue(new ItemStack(Items.GOLDEN_BOOTS).getMaxDamage() == 13 * 11, "boots");
        context.assertFalse(new ItemStack(Items.GOLDEN_HORSE_ARMOR).isDamageable(), "horse armor stays unbreakable");
        context.complete();
    }

    private static void assertResult(TestContext context, Identifier id, Item item, int count) {
        RecipeEntry<?> entry = context.getWorld().getRecipeManager().get(id)
                .orElseThrow(() -> new AssertionError("missing recipe " + id));
        ItemStack result = entry.value().getResult(context.getWorld().getRegistryManager());
        context.assertTrue(result.isOf(item) && result.getCount() == count, id + " produces " + result);
    }
}
