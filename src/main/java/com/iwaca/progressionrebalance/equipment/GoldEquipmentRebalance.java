package com.iwaca.progressionrebalance.equipment;

import com.iwaca.progressionrebalance.config.ConfigManager;
import com.iwaca.progressionrebalance.config.ModConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Turns gold into a fast, highly enchantable, high-performance but fragile equipment tier.
 *
 * <p>Only default item components are changed, through Fabric's {@link DefaultItemComponentEvents}:
 * <ul>
 *     <li><b>Tools</b> (pickaxe, axe, shovel, hoe): durability ({@code max_damage}) and, optionally, the harvest
 *     tier inside the {@code tool} component. Mining speed and enchantability stay vanilla.</li>
 *     <li><b>Swords</b>: their own, higher durability, since a weapon wears out per hit rather than per block.</li>
 *     <li><b>Golden sword and golden axe</b>: attack damage and attack speed, by rewriting the vanilla
 *     {@code base_attack_damage} / {@code base_attack_speed} modifiers of their {@code attribute_modifiers}.</li>
 *     <li><b>Armor</b>: durability, and armor points by rewriting the {@code armor} modifier. Toughness stays 0
 *     and there is no knockback resistance, so diamond still takes large hits far better.</li>
 * </ul>
 * Durability follows the vanilla gold materials, so modded tools and armor built on them are treated
 * consistently. Combat stats are only changed on the two vanilla weapons, and armor points only on gold armor
 * whose modifiers still come from the material (an empty {@code attribute_modifiers} component), so modded
 * items with their own stats keep them. Existing items pick up the new values automatically because stacks only
 * store changes to the defaults.
 */
public final class GoldEquipmentRebalance {
    /** Base values of a player, which item tooltips add to the item's modifiers. */
    static final double PLAYER_BASE_ATTACK_DAMAGE = 1.0;
    static final double PLAYER_BASE_ATTACK_SPEED = 4.0;

    private GoldEquipmentRebalance() {
    }

    public static void register() {
        ModConfig.Gold config = ConfigManager.get().gold();
        if (!config.enabled()) {
            return;
        }
        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(GoldEquipmentRebalance::isGoldTool, (builder, item) -> modifyTool(builder, item, config));
            context.modify(GoldEquipmentRebalance::isGoldArmor, (builder, item) -> modifyArmor(builder, item, config));
            context.modify(Items.GOLDEN_SWORD, builder -> builder.add(DataComponentTypes.ATTRIBUTE_MODIFIERS,
                    withBaseAttack(Items.GOLDEN_SWORD.getComponents(), config.swordAttackDamage(), config.swordAttackSpeed())));
            context.modify(Items.GOLDEN_AXE, builder -> builder.add(DataComponentTypes.ATTRIBUTE_MODIFIERS,
                    withBaseAttack(Items.GOLDEN_AXE.getComponents(), config.axeAttackDamage(), config.axeAttackSpeed())));
        });
    }

    static boolean isGoldTool(Item item) {
        return item instanceof ToolItem tool && tool.getMaterial() == ToolMaterials.GOLD
                && item.getComponents().contains(DataComponentTypes.MAX_DAMAGE);
    }

    static boolean isGoldArmor(Item item) {
        // Horse armor is also a gold ArmorItem but has no durability; only damageable pieces are changed.
        return item instanceof ArmorItem armor && armor.getMaterial().equals(ArmorMaterials.GOLD)
                && item.getComponents().contains(DataComponentTypes.MAX_DAMAGE);
    }

    private static void modifyTool(ComponentMap.Builder builder, Item item, ModConfig.Gold config) {
        builder.add(DataComponentTypes.MAX_DAMAGE, item instanceof SwordItem ? config.swordDurability() : config.toolDurability());
        ToolComponent tool = item.getComponents().get(DataComponentTypes.TOOL);
        if (config.ironMiningTier() && tool != null) {
            builder.add(DataComponentTypes.TOOL, withHarvestTier(tool, BlockTags.INCORRECT_FOR_GOLD_TOOL, BlockTags.INCORRECT_FOR_IRON_TOOL));
        }
    }

    private static void modifyArmor(ComponentMap.Builder builder, Item item, ModConfig.Gold config) {
        ArmorItem armor = (ArmorItem) item;
        builder.add(DataComponentTypes.MAX_DAMAGE, armor.getType().getMaxDamage(config.armorDurabilityMultiplier()));
        // Vanilla armor has an empty attribute_modifiers component, so stacks fall back to the material's
        // modifiers (ItemStack#applyAttributeModifiers). An item that defines its own modifiers keeps them.
        if (item.getComponents().getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT).modifiers().isEmpty()) {
            builder.add(DataComponentTypes.ATTRIBUTE_MODIFIERS, withArmor(armor.getAttributeModifiers(), armorPoints(armor.getType(), config)));
        }
    }

    static int armorPoints(ArmorItem.Type type, ModConfig.Gold config) {
        return switch (type) {
            case HELMET -> config.helmetArmor();
            case CHESTPLATE -> config.chestplateArmor();
            case LEGGINGS -> config.leggingsArmor();
            case BOOTS -> config.bootsArmor();
            case BODY -> throw new IllegalArgumentException("body armor has no durability and is not rebalanced");
        };
    }

    /**
     * Sets the weapon's attack damage and speed, given as the totals a player sees in the tooltip. Only the vanilla
     * base modifiers are rewritten; any other modifier on the item is kept.
     */
    static AttributeModifiersComponent withBaseAttack(ComponentMap components, double attackDamage, double attackSpeed) {
        AttributeModifiersComponent modifiers = components.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>(modifiers.modifiers().size());
        for (AttributeModifiersComponent.Entry entry : modifiers.modifiers()) {
            Identifier id = entry.modifier().id();
            if (entry.attribute().equals(EntityAttributes.GENERIC_ATTACK_DAMAGE) && id.equals(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID)) {
                entries.add(withValue(entry, attackDamage - PLAYER_BASE_ATTACK_DAMAGE));
            } else if (entry.attribute().equals(EntityAttributes.GENERIC_ATTACK_SPEED) && id.equals(Item.BASE_ATTACK_SPEED_MODIFIER_ID)) {
                entries.add(withValue(entry, attackSpeed - PLAYER_BASE_ATTACK_SPEED));
            } else {
                entries.add(entry);
            }
        }
        return new AttributeModifiersComponent(entries, modifiers.showInTooltip());
    }

    /** Sets the armor points of an armor piece's modifiers, keeping toughness and everything else. */
    static AttributeModifiersComponent withArmor(AttributeModifiersComponent modifiers, int armorPoints) {
        List<AttributeModifiersComponent.Entry> entries = new ArrayList<>(modifiers.modifiers().size());
        for (AttributeModifiersComponent.Entry entry : modifiers.modifiers()) {
            entries.add(entry.attribute().equals(EntityAttributes.GENERIC_ARMOR) ? withValue(entry, armorPoints) : entry);
        }
        return new AttributeModifiersComponent(entries, modifiers.showInTooltip());
    }

    private static AttributeModifiersComponent.Entry withValue(AttributeModifiersComponent.Entry entry, double value) {
        EntityAttributeModifier modifier = entry.modifier();
        return new AttributeModifiersComponent.Entry(entry.attribute(),
                new EntityAttributeModifier(modifier.id(), value, modifier.operation()), entry.slot());
    }

    /** Replaces the "never drops" rule for one tier's incorrect-blocks tag with another tier's tag. */
    static ToolComponent withHarvestTier(ToolComponent tool, TagKey<Block> from, TagKey<Block> to) {
        List<ToolComponent.Rule> rules = new ArrayList<>(tool.rules().size());
        for (ToolComponent.Rule rule : tool.rules()) {
            boolean isTierRule = rule.blocks().getTagKey().equals(Optional.of(from));
            rules.add(isTierRule
                    ? new ToolComponent.Rule(Registries.BLOCK.getOrCreateEntryList(to), rule.speed(), rule.correctForDrops())
                    : rule);
        }
        return new ToolComponent(rules, tool.defaultMiningSpeed(), tool.damagePerBlock());
    }
}
