package com.iwaca.progressionrebalance.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.iwaca.progressionrebalance.config.ModConfig;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Runs on the vanilla item definitions: mod entrypoints do not run in unit tests. */
class GoldEquipmentRebalanceTest {
    private static final double EPSILON = 1.0e-6;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void swordStatsAreTheTooltipTotals() {
        AttributeModifiersComponent sword = GoldEquipmentRebalance.withBaseAttack(Items.GOLDEN_SWORD.getComponents(), 6.5, 1.8);
        assertEquals(6.5, total(sword, EntityAttributes.GENERIC_ATTACK_DAMAGE, GoldEquipmentRebalance.PLAYER_BASE_ATTACK_DAMAGE, EquipmentSlot.MAINHAND), EPSILON);
        assertEquals(1.8, total(sword, EntityAttributes.GENERIC_ATTACK_SPEED, GoldEquipmentRebalance.PLAYER_BASE_ATTACK_SPEED, EquipmentSlot.MAINHAND), EPSILON);
        assertEquals(2, sword.modifiers().size(), "no modifier is added or lost");
        assertTrue(sword.showInTooltip());
    }

    @Test
    void goldSwordSitsBetweenIronAndDiamondPerHitButSwingsFaster() {
        ModConfig.Gold gold = ModConfig.defaults().gold();
        double vanillaGold = playerAttackDamage(Items.GOLDEN_SWORD);
        double iron = playerAttackDamage(Items.IRON_SWORD);
        double diamond = playerAttackDamage(Items.DIAMOND_SWORD);
        double ironSpeed = total(component(Items.IRON_SWORD), EntityAttributes.GENERIC_ATTACK_SPEED, GoldEquipmentRebalance.PLAYER_BASE_ATTACK_SPEED, EquipmentSlot.MAINHAND);
        assertEquals(4.0, vanillaGold, EPSILON, "vanilla golden sword, for reference");
        assertEquals(6.0, iron, EPSILON);
        assertEquals(7.0, diamond, EPSILON);
        assertEquals(1.6, ironSpeed, EPSILON);
        assertTrue(gold.swordAttackDamage() > iron && gold.swordAttackDamage() < diamond);
        assertTrue(gold.swordAttackSpeed() > ironSpeed);
    }

    @Test
    void axeKeepsItsOtherModifiers() {
        AttributeModifiersComponent axe = GoldEquipmentRebalance.withBaseAttack(Items.GOLDEN_AXE.getComponents(), 8.0, 1.1);
        assertEquals(8.0, total(axe, EntityAttributes.GENERIC_ATTACK_DAMAGE, GoldEquipmentRebalance.PLAYER_BASE_ATTACK_DAMAGE, EquipmentSlot.MAINHAND), EPSILON);
        assertEquals(1.1, total(axe, EntityAttributes.GENERIC_ATTACK_SPEED, GoldEquipmentRebalance.PLAYER_BASE_ATTACK_SPEED, EquipmentSlot.MAINHAND), EPSILON);
        assertEquals(component(Items.GOLDEN_AXE).modifiers().size(), axe.modifiers().size());
    }

    @Test
    void armorPointsChangeButToughnessStaysZero() {
        ModConfig.Gold gold = ModConfig.defaults().gold();
        double armor = 0.0;
        double toughness = 0.0;
        for (ArmorItem piece : new ArmorItem[] {
                (ArmorItem) Items.GOLDEN_HELMET, (ArmorItem) Items.GOLDEN_CHESTPLATE,
                (ArmorItem) Items.GOLDEN_LEGGINGS, (ArmorItem) Items.GOLDEN_BOOTS}) {
            AttributeModifiersComponent rebalanced = GoldEquipmentRebalance.withArmor(
                    piece.getAttributeModifiers(), GoldEquipmentRebalance.armorPoints(piece.getType(), gold));
            EquipmentSlot slot = piece.getSlotType();
            armor += total(rebalanced, EntityAttributes.GENERIC_ARMOR, 0.0, slot);
            toughness += total(rebalanced, EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 0.0, slot);
            assertEquals(0.0, total(rebalanced, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.0, slot));
        }
        assertEquals(18.0, armor, EPSILON, "3 + 7 + 5 + 3");
        assertEquals(0.0, toughness, "gold has no armor toughness");
    }

    private static AttributeModifiersComponent component(Item item) {
        return item.getComponents().getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
    }

    private static double playerAttackDamage(Item item) {
        return total(component(item), EntityAttributes.GENERIC_ATTACK_DAMAGE, GoldEquipmentRebalance.PLAYER_BASE_ATTACK_DAMAGE, EquipmentSlot.MAINHAND);
    }

    private static double total(AttributeModifiersComponent modifiers, RegistryEntry<EntityAttribute> attribute, double base, EquipmentSlot slot) {
        double[] value = {base};
        modifiers.applyModifiers(slot, (entry, modifier) -> {
            if (entry.equals(attribute)) {
                value[0] += modifier.value();
            }
        });
        return value[0];
    }
}
