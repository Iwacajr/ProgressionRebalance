package com.iwaca.progressionrebalance.potion;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/**
 * A water cauldron with potions poured into it. The mixture lives in a {@link PotionCauldronBlockEntity};
 * the level is still the amount of water.
 *
 * <p>It reuses the vanilla water cauldron models (tinted with the mixture's color on the client) and, through
 * the copied block settings, the water cauldron's loot table, so breaking it drops a plain cauldron.
 * Precipitation {@code NONE} keeps rain and dripstone from filling it.
 */
public class PotionCauldronBlock extends LeveledCauldronBlock implements BlockEntityProvider {
    public static final MapCodec<LeveledCauldronBlock> CODEC = createCodec(PotionCauldronBlock::new);

    public PotionCauldronBlock(Settings settings) {
        super(Biome.Precipitation.NONE, PotionCauldrons.BEHAVIOR, settings);
    }

    @Override
    public MapCodec<LeveledCauldronBlock> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PotionCauldronBlockEntity(pos, state);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron) {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
            world.addParticle(EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, cauldron.color()),
                    x, pos.getY() + getFluidHeight(state), z, 0.0, 0.0, 0.0);
        }
    }
}
