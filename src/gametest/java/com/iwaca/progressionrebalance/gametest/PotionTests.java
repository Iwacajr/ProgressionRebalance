package com.iwaca.progressionrebalance.gametest;

import com.iwaca.progressionrebalance.potion.DurationContributions;
import com.iwaca.progressionrebalance.potion.ModPotions;
import com.iwaca.progressionrebalance.potion.PotionCauldronBlockEntity;
import com.iwaca.progressionrebalance.potion.PotionCauldrons;
import java.util.List;
import java.util.stream.StreamSupport;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public class PotionTests implements FabricGameTest {
    private static final BlockPos CAULDRON = new BlockPos(1, 1, 1);

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void brewedDurationsFollowThePolicy(TestContext context) {
        expectDuration(context, Potions.NIGHT_VISION, 7200);         // utility x2
        expectDuration(context, Potions.LONG_NIGHT_VISION, 19200);   // utility x2
        expectDuration(context, Potions.LUCK, 12000);                // utility x2
        expectDuration(context, Potions.STRENGTH, 5400);             // combat x1.5
        expectDuration(context, Potions.REGENERATION, 1350);         // combat x1.5
        expectDuration(context, Potions.STRONG_STRENGTH, 1800);      // level II unchanged
        expectDuration(context, Potions.STRONG_SWIFTNESS, 1800);     // level II unchanged
        expectDuration(context, Potions.POISON, 900);                // harmful unchanged
        expectDuration(context, Potions.TURTLE_MASTER, 400);         // amplified unchanged
        expectDuration(context, ModPotions.RESISTANCE, 3600);
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void onlyDrinkablePotionsStack(TestContext context) {
        context.assertTrue(new ItemStack(Items.POTION).getMaxCount() == 4, "drinkable potions stack to 4");
        context.assertTrue(new ItemStack(Items.SPLASH_POTION).getMaxCount() == 1, "splash potions do not stack");
        context.assertTrue(new ItemStack(Items.LINGERING_POTION).getMaxCount() == 1, "lingering potions do not stack");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void potionsArePouredInAndBottledAsOneMixture(TestContext context) {
        context.setBlockState(CAULDRON, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);

        hold(player, Items.POTION, Potions.LONG_SWIFTNESS);
        context.assertTrue(use(context, player).isAccepted(), "pouring into a water cauldron succeeds");
        context.assertTrue(player.getMainHandStack().isOf(Items.GLASS_BOTTLE), "the poured potion's bottle is returned");
        context.assertTrue(context.getBlockState(CAULDRON).isOf(PotionCauldrons.BLOCK), "the cauldron now holds a potion");
        context.assertTrue(level(context) == 3, "pouring does not change the water level");

        hold(player, Items.POTION, Potions.LONG_NIGHT_VISION);
        context.assertTrue(use(context, player).isAccepted(), "second potion is poured in");
        hold(player, Items.POTION, Potions.LONG_FIRE_RESISTANCE);
        context.assertTrue(use(context, player).isAccepted(), "third potion is poured in");
        context.assertTrue(cauldron(context).color() == PotionContentsComponent.getColor(cauldron(context).contents().getEffects()),
                "the liquid is tinted with the mixture's color");

        hold(player, Items.POTION, Potions.WATER_BREATHING);
        context.assertTrue(use(context, player) == ActionResult.CONSUME, "a fourth effect is rejected");
        context.assertTrue(player.getMainHandStack().isOf(Items.POTION), "the rejected potion is kept");
        hold(player, Items.SPLASH_POTION, Potions.STRENGTH);
        context.assertTrue(use(context, player) == ActionResult.CONSUME, "a splash potion does not mix with drinkable potions");

        player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
        context.assertTrue(use(context, player).isAccepted(), "an empty bottle takes the mixture out");
        ItemStack mixture = player.getMainHandStack();
        List<StatusEffectInstance> effects = effects(mixture);
        context.assertTrue(mixture.isOf(Items.POTION) && effects.size() == 3, "one potion with all three effects: " + effects);
        int longDuration = Potions.LONG_SWIFTNESS.value().getEffects().getFirst().getDuration();
        context.assertTrue(effects.stream().allMatch(e -> e.getDuration() == longDuration), "equal durations average to themselves");
        context.assertTrue(mixture.getName().getString().equals("Mixed Potion"), "named Mixed Potion, not Uncraftable: " + mixture.getName().getString());
        context.assertTrue(context.getBlockState(CAULDRON).isOf(Blocks.WATER_CAULDRON) && level(context) == 2,
                "bottling uses one level and leaves plain water");
        context.complete();
    }

    /** Brewed durations: Long Night Vision 16:00, Long Strength 12:00, Slow Falling 3:00, Swiftness 6:00. */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void chainedMixingAveragesEveryOriginalDuration(TestContext context) {
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        context.setBlockState(CAULDRON, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
        hold(player, Items.POTION, Potions.LONG_NIGHT_VISION);
        use(context, player);
        hold(player, Items.POTION, Potions.LONG_STRENGTH);
        use(context, player);
        ItemStack firstMix = bottle(context, player);
        context.assertTrue(effects(firstMix).stream().allMatch(e -> e.getDuration() == 16800), "16:00 + 12:00 = 14:00: " + effects(firstMix));

        // The bottled mixture survives an item save and load with its contributions.
        RegistryWrapper.WrapperLookup registries = context.getWorld().getRegistryManager();
        ItemStack reloaded = ItemStack.fromNbt(registries, firstMix.encode(registries)).orElseThrow();
        context.assertTrue(DurationContributions.of(reloaded).equals(new DurationContributions(33600L, 2)),
                "contributions are saved with the potion: " + DurationContributions.of(reloaded));

        context.setBlockState(CAULDRON, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
        hold(player, Items.POTION, Potions.SLOW_FALLING);
        use(context, player);
        player.setStackInHand(Hand.MAIN_HAND, reloaded);
        context.assertTrue(use(context, player).isAccepted(), "a mixed potion can be poured into another mixture");
        ItemStack secondMix = bottle(context, player);
        List<StatusEffectInstance> effects = effects(secondMix);
        context.assertTrue(effects.size() == 3 && effects.stream().allMatch(e -> e.getDuration() == 12400),
                "(16 + 12 + 3) / 3 = 10:20, not (14 + 3) / 2 = 8:30: " + effects);
        context.assertTrue(effects.stream().anyMatch(e -> e.getEffectType().equals(StatusEffects.STRENGTH) && e.getAmplifier() == 0),
                "amplifiers are unchanged");

        context.setBlockState(CAULDRON, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
        hold(player, Items.POTION, Potions.LONG_NIGHT_VISION);
        use(context, player);
        hold(player, Items.POTION, Potions.SWIFTNESS);
        use(context, player);
        hold(player, Items.POTION, Potions.STRONG_HEALING);
        context.assertTrue(use(context, player).isAccepted(), "an instant potion can be mixed in");
        List<StatusEffectInstance> withHealing = effects(bottle(context, player));
        context.assertTrue(withHealing.stream().filter(e -> !e.getEffectType().value().isInstant()).allMatch(e -> e.getDuration() == 13200),
                "(16:00 + 6:00) / 2 = 11:00; healing adds no duration: " + withHealing);
        context.assertTrue(withHealing.stream().anyMatch(e -> e.getEffectType().equals(StatusEffects.INSTANT_HEALTH) && e.getAmplifier() == 1),
                "Instant Health II stays instant and level II: " + withHealing);
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void singlePotionComesBackUnchangedAndEmptiesTheLastLevel(TestContext context) {
        context.setBlockState(CAULDRON, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 1));
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        hold(player, Items.LINGERING_POTION, Potions.SLOW_FALLING);
        context.assertTrue(use(context, player).isAccepted(), "lingering potions can be poured in too");

        player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
        context.assertTrue(use(context, player).isAccepted(), "bottling succeeds");
        ItemStack bottled = player.getMainHandStack();
        context.assertTrue(ItemStack.areEqual(bottled, PotionContentsComponent.createStack(Items.LINGERING_POTION, Potions.SLOW_FALLING)),
                "a single potion comes back as itself: " + bottled);
        context.assertTrue(context.getBlockState(CAULDRON).isOf(Blocks.CAULDRON), "the last water level is used up");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void mixtureSurvivesSavingAndLoading(TestContext context) {
        context.setBlockState(CAULDRON, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 2));
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        hold(player, Items.SPLASH_POTION, Potions.SWIFTNESS);
        use(context, player);
        hold(player, Items.SPLASH_POTION, Potions.NIGHT_VISION);
        use(context, player);

        PotionCauldronBlockEntity original = cauldron(context);
        RegistryWrapper.WrapperLookup registries = context.getWorld().getRegistryManager();
        NbtCompound saved = original.createNbtWithIdentifyingData(registries);
        BlockEntity loaded = BlockEntity.createFromNbt(original.getPos(), original.getCachedState(), saved, registries);
        context.assertTrue(loaded instanceof PotionCauldronBlockEntity copy
                        && copy.kind() == Items.SPLASH_POTION && copy.contents().equals(original.contents())
                        && copy.durations().equals(original.durations()) && copy.durations().contributors() == 2,
                "kind, effects and duration contributions are saved: " + saved);
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void cauldronKeepsVanillaBehaviorForOtherItems(TestContext context) {
        context.setBlockState(CAULDRON, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 2));
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        hold(player, Items.POTION, Potions.AWKWARD);
        context.assertTrue(use(context, player) == ActionResult.PASS, "potions without effects are not poured in");
        hold(player, Items.POTION, Potions.WATER);
        context.assertTrue(use(context, player).isAccepted(), "water bottles still fill the cauldron");
        context.assertTrue(context.getBlockState(CAULDRON).isOf(Blocks.WATER_CAULDRON) && level(context) == 3, "vanilla water bottle behavior");
        context.complete();
    }

    private static void hold(PlayerEntity player, Item kind, RegistryEntry<Potion> potion) {
        player.setStackInHand(Hand.MAIN_HAND, PotionContentsComponent.createStack(kind, potion));
    }

    /** A main-hand use like the server does it: Fabric's event first, then the block's own item interaction. */
    private static ActionResult use(TestContext context, PlayerEntity player) {
        BlockPos pos = context.getAbsolutePos(CAULDRON);
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
        ActionResult event = UseBlockCallback.EVENT.invoker().interact(player, context.getWorld(), Hand.MAIN_HAND, hit);
        if (event != ActionResult.PASS) {
            return event;
        }
        BlockState state = context.getWorld().getBlockState(pos);
        return state.onUseWithItem(player.getMainHandStack(), context.getWorld(), player, Hand.MAIN_HAND, hit).toActionResult();
    }

    private static ItemStack bottle(TestContext context, PlayerEntity player) {
        player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
        context.assertTrue(use(context, player).isAccepted(), "an empty bottle takes the mixture out");
        return player.getMainHandStack();
    }

    private static int level(TestContext context) {
        BlockState state = context.getBlockState(CAULDRON);
        return state.contains(LeveledCauldronBlock.LEVEL) ? state.get(LeveledCauldronBlock.LEVEL) : 0;
    }

    private static PotionCauldronBlockEntity cauldron(TestContext context) {
        if (context.getBlockEntity(CAULDRON) instanceof PotionCauldronBlockEntity cauldron) {
            return cauldron;
        }
        throw new GameTestException("no potion cauldron at " + CAULDRON);
    }

    private static List<StatusEffectInstance> effects(ItemStack stack) {
        PotionContentsComponent contents = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        return StreamSupport.stream(contents.getEffects().spliterator(), false).toList();
    }

    private static void expectDuration(TestContext context, RegistryEntry<Potion> potion, int ticks) {
        int actual = potion.value().getEffects().getFirst().getDuration();
        context.assertTrue(actual == ticks, potion.getIdAsString() + " lasts " + actual + " ticks, expected " + ticks);
    }
}
