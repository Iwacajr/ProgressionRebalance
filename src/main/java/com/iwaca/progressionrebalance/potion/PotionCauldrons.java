package com.iwaca.progressionrebalance.potion;

import com.iwaca.progressionrebalance.ProgressionRebalance;
import com.iwaca.progressionrebalance.config.ConfigManager;
import java.util.Optional;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

/**
 * Potion mixing in cauldrons.
 *
 * <ol>
 *     <li>Use a potion on a water cauldron: it is poured in (the bottle comes back empty) and the cauldron
 *     becomes a potion cauldron tinted with the potion's color.</li>
 *     <li>Pour more potions of the same kind into it, following the {@link PotionMixing} rules. Every timed
 *     effect lasts the average duration of the potions poured in.</li>
 *     <li>Use an empty bottle on it to take the whole mixture out as one potion. That uses one level of water;
 *     the rest turns back into plain water.</li>
 * </ol>
 * One potion comes out for all the potions poured in, so mixing can never duplicate effects.
 *
 * <p>Pouring into a water cauldron uses Fabric's {@link UseBlockCallback} rather than vanilla's water cauldron
 * behavior map, which vanilla rebuilds during bootstrap (after mod initialization). The potion cauldron is this
 * mod's own block, so it has its own behavior map.
 */
public final class PotionCauldrons {
    public static final CauldronBehavior.CauldronBehaviorMap BEHAVIOR = CauldronBehavior.createMap(ProgressionRebalance.MOD_ID + ":potion");

    public static final PotionCauldronBlock BLOCK = Registry.register(Registries.BLOCK, ProgressionRebalance.id("potion_cauldron"),
            new PotionCauldronBlock(AbstractBlock.Settings.copy(Blocks.WATER_CAULDRON)));

    public static final BlockEntityType<PotionCauldronBlockEntity> BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ProgressionRebalance.id("potion_cauldron"), BlockEntityType.Builder.create(PotionCauldronBlockEntity::new, BLOCK).build(null));

    private PotionCauldrons() {
    }

    public static void register() {
        // Taking a mixture out always works, so existing potion cauldrons stay usable if mixing is disabled later.
        BEHAVIOR.map().put(Items.GLASS_BOTTLE, PotionCauldrons::bottleMixture);
        if (!ConfigManager.get().potions().mixingEnabled()) {
            return;
        }
        for (Item kind : PotionMixing.KINDS) {
            BEHAVIOR.map().put(kind, PotionCauldrons::pourIntoMixture);
        }
        UseBlockCallback.EVENT.register(PotionCauldrons::pourIntoWater);
    }

    private static ActionResult pourIntoWater(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        ItemStack stack = player.getStackInHand(hand);
        // Water bottles and potions without effects keep their vanilla behavior. Sneaking skips block
        // interactions as in vanilla, so potions can still be drunk next to a cauldron.
        if (!state.isOf(Blocks.WATER_CAULDRON) || player.isSpectator() || player.shouldCancelInteraction()
                || !PotionMixing.isMixablePotionItem(stack) || !PotionMixing.hasEffects(stack)) {
            return ActionResult.PASS;
        }
        if (!world.isClient) {
            Item kind = stack.getItem();
            PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
            world.setBlockState(pos, BLOCK.getDefaultState().with(LeveledCauldronBlock.LEVEL, state.get(LeveledCauldronBlock.LEVEL)));
            if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron) {
                cauldron.setMixture(kind, contents, DurationContributions.of(stack));
            }
            emptyBottleInto(player, hand, stack, world, pos);
        }
        // On the client, SUCCESS makes Fabric send the interaction to the server.
        return ActionResult.SUCCESS;
    }

    private static ItemActionResult pourIntoMixture(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (!PotionMixing.hasEffects(stack) || !(world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        int maxEffects = ConfigManager.get().potions().maxMixedEffects();
        PotionMixing.Result result = PotionMixing.pour(cauldron.kind(), cauldron.contents().getEffects(), cauldron.durations(), stack, maxEffects);
        if (!result.isSuccess()) {
            if (!world.isClient) {
                player.sendMessage(Text.translatable(result.rejection().translationKey(), maxEffects).formatted(Formatting.RED), true);
            }
            // Handled without effect: nothing is poured and the potion is not drunk instead.
            return ItemActionResult.CONSUME;
        }
        if (!world.isClient) {
            cauldron.setMixture(cauldron.kind(), new PotionContentsComponent(Optional.empty(), Optional.empty(), result.effects()), result.durations());
            emptyBottleInto(player, hand, stack, world, pos);
        }
        return ItemActionResult.success(world.isClient);
    }

    private static ItemActionResult bottleMixture(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (!(world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!world.isClient) {
            ItemStack potion = PotionMixing.bottle(cauldron.kind(), cauldron.contents(), cauldron.durations());
            Item bottle = stack.getItem();
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, potion));
            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(bottle));
            int water = state.get(LeveledCauldronBlock.LEVEL) - 1;
            BlockState remaining = water == 0
                    ? Blocks.CAULDRON.getDefaultState()
                    : Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, water);
            world.setBlockState(pos, remaining);
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return ItemActionResult.success(world.isClient);
    }

    private static void emptyBottleInto(PlayerEntity player, Hand hand, ItemStack stack, World world, BlockPos pos) {
        Item potion = stack.getItem();
        player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
        player.incrementStat(Stats.USE_CAULDRON);
        player.incrementStat(Stats.USED.getOrCreateStat(potion));
        world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
    }
}
