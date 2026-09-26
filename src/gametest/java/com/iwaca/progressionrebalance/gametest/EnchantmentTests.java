package com.iwaca.progressionrebalance.gametest;

import com.iwaca.progressionrebalance.enchantment.RebalanceTags;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.effect.AllOfEnchantmentEffects;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.enchantment.effect.TargetedEnchantmentEffect;
import net.minecraft.enchantment.effect.entity.DamageEntityEnchantmentEffect;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class EnchantmentTests implements FabricGameTest {
    private static final float EPSILON = 1.0e-4F;
    private static final float BASE_DAMAGE = 8.0F;

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void protectionIsWeakerButSpecializationsAreNot(TestContext context) {
        DamageSource general = context.getWorld().getDamageSources().generic();
        DamageSource fire = context.getWorld().getDamageSources().inFire();
        DamageSource explosion = context.getWorld().getDamageSources().explosion(null, null);
        DamageSource arrow = context.getWorld().getDamageSources().arrow(null, null);

        ArmorStandEntity full = armored(context, 1, Enchantments.PROTECTION, Enchantments.PROTECTION, Enchantments.PROTECTION, Enchantments.PROTECTION);
        expectProtection(context, full, general, 12.0F, "4x Protection IV: 12 points = 48%");

        ArmorStandEntity threeAndFire = armored(context, 3, Enchantments.PROTECTION, Enchantments.PROTECTION, Enchantments.PROTECTION, Enchantments.FIRE_PROTECTION);
        expectProtection(context, threeAndFire, general, 9.0F, "3x Protection IV: 36% against general damage");
        expectProtection(context, threeAndFire, fire, 17.0F, "3x Protection IV + Fire Protection IV: 68% against fire");

        for (Map.Entry<RegistryKey<Enchantment>, DamageSource> specialization : Map.of(
                Enchantments.FIRE_PROTECTION, fire, Enchantments.BLAST_PROTECTION, explosion, Enchantments.PROJECTILE_PROTECTION, arrow).entrySet()) {
            RegistryKey<Enchantment> special = specialization.getKey();
            ArmorStandEntity split = armored(context, 5, Enchantments.PROTECTION, Enchantments.PROTECTION, special, special);
            expectProtection(context, split, general, 6.0F, "2x Protection IV: 24% against general damage");
            expectProtection(context, split, specialization.getValue(), 22.0F, "2x Protection IV + 2x " + special.getValue().getPath() + " IV");
            float taken = DamageUtil.getInflictedDamage(10.0F,
                    EnchantmentHelper.getProtectionAmount(context.getWorld(), split, specialization.getValue()));
            context.assertTrue(Math.abs(taken - 2.0F) < EPSILON, "the vanilla 80% cap still applies: took " + taken + " of 10");
            split.discard();
        }
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void thornsHitsHarderAndLeavesTheCooldownAlone(TestContext context) {
        DamageEntityEnchantmentEffect thorns = thornsDamage(context);
        context.assertTrue(Math.abs(thorns.minDamage().getValue(3) - 1.35F) < EPSILON && Math.abs(thorns.maxDamage().getValue(3) - 6.75F) < EPSILON,
                "Thorns reflects 1.35 to 6.75 instead of 1 to 5: " + thorns);

        PlayerEntity wearer = context.createMockPlayer(GameMode.SURVIVAL);
        DamageSource sword = context.getWorld().getDamageSources().playerAttack(wearer);

        // Thorns on a fresh attacker must not start a cooldown that absorbs the next sword hit.
        IronGolemEntity attacker = golem(context, 1);
        float thornsDamage = damageFromThorns(context, thorns, wearer, attacker);
        context.assertTrue(thornsDamage > 0.0F, "Thorns deals real damage");
        context.assertTrue(attacker.timeUntilRegen == 0, "Thorns does not start the damage cooldown");
        float health = attacker.getHealth();
        attacker.damage(sword, 5.0F);
        context.assertTrue(Math.abs(health - attacker.getHealth() - 5.0F) < EPSILON, "the next sword hit deals full damage");

        // During a cooldown started by a normal hit, Thorns neither is absorbed nor changes that cooldown.
        IronGolemEntity hit = golem(context, 4);
        hit.damage(sword, 5.0F);
        int cooldown = hit.timeUntilRegen;
        context.assertTrue(damageFromThorns(context, thorns, wearer, hit) > 0.0F, "Thorns is not absorbed by the cooldown");
        context.assertTrue(hit.timeUntilRegen == cooldown, "the cooldown is untouched");
        health = hit.getHealth();
        hit.damage(sword, 4.0F);
        context.assertTrue(hit.getHealth() == health, "normal cooldowns still absorb weaker follow-up hits");

        // Thorns-type damage that does not come from the enchantment (Guardian spikes) keeps vanilla cooldowns.
        IronGolemEntity spiked = golem(context, 7);
        spiked.damage(context.getWorld().getDamageSources().thorns(wearer), 2.0F);
        context.assertTrue(spiked.timeUntilRegen == 20, "only enchantment Thorns bypasses the cooldown");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void baneCoversMonstersButNotHumanoids(TestContext context) {
        for (EntityType<?> type : List.of(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.ENDERMITE,
                EntityType.CREEPER, EntityType.SLIME, EntityType.MAGMA_CUBE, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN,
                EntityType.RAVAGER, EntityType.BEE)) {
            context.assertTrue(type.isIn(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS), type.getUntranslatedName() + " is a Bane target");
        }
        context.assertTrue(EntityType.CREEPER.isIn(RebalanceTags.BANE_TARGETS), "the mod's tag holds the new targets");
        context.assertFalse(EntityType.ZOMBIE.isIn(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS), "zombies are not Bane targets");

        ItemStack bane = enchanted(context, Items.IRON_SWORD, Enchantments.BANE_OF_ARTHROPODS, 5);
        expectBonus(context, bane, mob(context, EntityType.CREEPER, 1), 12.5F, "Bane V vs creeper: vanilla +12.5");
        expectBonus(context, bane, mob(context, EntityType.RAVAGER, 4), 12.5F, "Bane V vs ravager");
        expectBonus(context, bane, mob(context, EntityType.ZOMBIE, 7), 0.0F, "Bane V vs zombie: no bonus");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void impalingIsStrongerAgainstWetTargets(TestContext context) {
        BlockPos pool = new BlockPos(4, 1, 1);
        context.setBlockState(pool, Blocks.WATER);
        context.setBlockState(pool.up(), Blocks.WATER);
        ItemStack trident = enchanted(context, Items.TRIDENT, Enchantments.IMPALING, 5);
        MobEntity dry = mob(context, EntityType.ZOMBIE, 1);
        MobEntity wet = mob(context, EntityType.ZOMBIE, 4);
        MobEntity fish = mob(context, EntityType.COD, 7);
        context.addInstantFinalTask(() -> {
            context.assertTrue(wet.isTouchingWater(), "zombie is not in the water yet");
            expectBonus(context, trident, dry, 0.0F, "Impaling V vs dry zombie: no bonus");
            expectBonus(context, trident, wet, 7.5F, "Impaling V vs zombie in water: 60% of +12.5");
            expectBonus(context, trident, fish, 12.5F, "Impaling V vs cod: full vanilla bonus, not stacked with the wet bonus");
        });
    }

    private static DamageEntityEnchantmentEffect thornsDamage(TestContext context) {
        Enchantment thorns = entry(context, Enchantments.THORNS).value();
        for (TargetedEnchantmentEffect<EnchantmentEntityEffect> effect : thorns.getEffect(EnchantmentEffectComponentTypes.POST_ATTACK)) {
            if (effect.effect() instanceof AllOfEnchantmentEffects.EntityEffects allOf) {
                for (EnchantmentEntityEffect inner : allOf.effects()) {
                    if (inner instanceof DamageEntityEnchantmentEffect damage) {
                        return damage;
                    }
                }
            }
        }
        throw new AssertionError("Thorns has no damage effect");
    }

    /** Applies the loaded Thorns damage effect (as when Thorns triggers) and returns the health it took. */
    private static float damageFromThorns(TestContext context, DamageEntityEnchantmentEffect thorns, PlayerEntity wearer, LivingEntity attacker) {
        float health = attacker.getHealth();
        EnchantmentEffectContext effectContext = new EnchantmentEffectContext(new ItemStack(Items.IRON_CHESTPLATE), EquipmentSlot.CHEST, wearer);
        thorns.apply(context.getWorld(), 3, effectContext, attacker, attacker.getPos());
        return health - attacker.getHealth();
    }

    private static IronGolemEntity golem(TestContext context, int x) {
        IronGolemEntity golem = context.spawnEntity(EntityType.IRON_GOLEM, x, 1, 5);
        golem.setAiDisabled(true);
        return golem;
    }

    private static <T extends MobEntity> T mob(TestContext context, EntityType<T> type, int x) {
        T mob = context.spawnEntity(type, x, 1, 1);
        mob.setAiDisabled(true);
        return mob;
    }

    private static ArmorStandEntity armored(TestContext context, int x, RegistryKey<Enchantment> head, RegistryKey<Enchantment> chest,
                                            RegistryKey<Enchantment> legs, RegistryKey<Enchantment> feet) {
        ArmorStandEntity stand = context.spawnEntity(EntityType.ARMOR_STAND, x, 1, 3);
        stand.equipStack(EquipmentSlot.HEAD, enchanted(context, Items.IRON_HELMET, head, 4));
        stand.equipStack(EquipmentSlot.CHEST, enchanted(context, Items.IRON_CHESTPLATE, chest, 4));
        stand.equipStack(EquipmentSlot.LEGS, enchanted(context, Items.IRON_LEGGINGS, legs, 4));
        stand.equipStack(EquipmentSlot.FEET, enchanted(context, Items.IRON_BOOTS, feet, 4));
        return stand;
    }

    private static void expectProtection(TestContext context, LivingEntity entity, DamageSource source, float points, String what) {
        float actual = EnchantmentHelper.getProtectionAmount(context.getWorld(), entity, source);
        context.assertTrue(Math.abs(actual - points) < EPSILON, what + ": expected " + points + " protection points, got " + actual);
    }

    private static void expectBonus(TestContext context, ItemStack weapon, LivingEntity target, float bonus, String what) {
        DamageSource source = context.getWorld().getDamageSources().generic();
        float damage = EnchantmentHelper.getDamage(context.getWorld(), weapon, target, source, BASE_DAMAGE);
        context.assertTrue(Math.abs(damage - BASE_DAMAGE - bonus) < EPSILON, what + ": expected +" + bonus + ", got +" + (damage - BASE_DAMAGE));
    }

    private static ItemStack enchanted(TestContext context, Item item, RegistryKey<Enchantment> enchantment, int level) {
        ItemStack stack = new ItemStack(item);
        stack.addEnchantment(entry(context, enchantment), level);
        return stack;
    }

    private static RegistryEntry<Enchantment> entry(TestContext context, RegistryKey<Enchantment> key) {
        return context.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(key).orElseThrow();
    }
}
