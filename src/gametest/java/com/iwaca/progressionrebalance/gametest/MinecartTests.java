package com.iwaca.progressionrebalance.gametest;

import com.iwaca.progressionrebalance.config.ConfigManager;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.RailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

/**
 * Minecart behavior on long tracks at the configured top speed.
 *
 * <p>Tracks run towards -z (and -x), away from the other tests, and each test has its own batch. The GameTest
 * server only ticks the chunks around a test's structure, so the chunks under the track are force-loaded
 * (the test world is discarded afterwards); otherwise the cart would simply freeze at the first chunk border.
 * Tracks end at a solid block, so a cart that got through stays on the last rail. Tracks are built high above
 * the test structure, see {@link #TRACK_Y}.
 *
 * <p>Note: vanilla's {@code TestContext.getRelativePos} applies an extra 180 degree rotation in 1.21.1, so
 * these tests compare absolute positions instead.
 */
public class MinecartTests implements FabricGameTest {
    private static final int X = 1;
    private static final int TRACK_LENGTH = 40;
    private static final int LAUNCH_TICK = 5;
    private static final double VANILLA_BLOCKS_PER_TICK = 0.4;
    private static final int CURVE_TRACKS = 4;
    /**
     * Height of the tracks above the test structure. The GameTest runner clears the area around other tests'
     * structures (up to 20 blocks above them) between batches, which would cut a long track at ground level.
     */
    private static final int TRACK_Y = 40;

    @GameTest(templateName = EMPTY_STRUCTURE, batchId = "minecart_straight", tickLimit = 200)
    public void straightPoweredTrackReachesTheConfiguredSpeed(TestContext context) {
        for (int z = 0; z >= -TRACK_LENGTH; z--) {
            poweredRail(context, at(X, 1, z), RailShape.NORTH_SOUTH);
        }
        MinecartEntity[] cart = launchLater(context, new Vec3d(X, 1, 0));
        double expected = VANILLA_BLOCKS_PER_TICK * ConfigManager.get().transport().minecartSpeedMultiplier();
        double[] previousZ = {Double.NaN};
        int[] ticksAtSpeed = {0};
        // Runs every tick until it passes: the cart must cover the configured distance per tick for 10 ticks in a row.
        context.addInstantFinalTask(() -> {
            context.assertTrue(cart[0] != null, "cart not launched yet");
            double moved = previousZ[0] - cart[0].getZ();
            previousZ[0] = cart[0].getZ();
            ticksAtSpeed[0] = Math.abs(moved - expected) < 0.01 && cart[0].isOnRail() ? ticksAtSpeed[0] + 1 : 0;
            context.assertTrue(ticksAtSpeed[0] >= 10, "cart moved " + moved + " blocks in the last tick, expected " + expected);
        });
    }

    /**
     * Where a cart enters a curve depends on its speed and start position, so several carts start at different
     * offsets and approach their curve over different distances. Every one of them must take its curve.
     */
    @GameTest(templateName = EMPTY_STRUCTURE, batchId = "minecart_curve", tickLimit = 200)
    public void fullSpeedCurvesDoNotDerail(TestContext context) {
        MinecartEntity[][] carts = new MinecartEntity[CURVE_TRACKS][];
        BlockPos[] afterCurve = new BlockPos[CURVE_TRACKS];
        for (int track = 0; track < CURVE_TRACKS; track++) {
            // Tracks further east turn later, so each westward branch passes south of the previous track's end.
            int x = X + 4 * track;
            int cornerZ = -20 - 2 * track;
            for (int z = 0; z > cornerZ; z--) {
                poweredRail(context, at(x, 1, z), RailShape.NORTH_SOUTH);
            }
            rail(context, at(x, 1, cornerZ), RailShape.SOUTH_WEST);
            for (int west = x - 1; west >= x - 6; west--) {
                rail(context, at(west, 1, cornerZ), RailShape.EAST_WEST);
            }
            context.setBlockState(at(x - 7, 1, cornerZ), Blocks.STONE);
            afterCurve[track] = context.getAbsolutePos(at(x - 4, 1, cornerZ));
            carts[track] = launchLater(context, new Vec3d(x, 1, -0.25 * track));
        }
        context.addInstantFinalTask(() -> {
            for (int track = 0; track < CURVE_TRACKS; track++) {
                MinecartEntity cart = carts[track][0];
                context.assertTrue(cart != null, "cart not launched yet");
                context.assertTrue(cart.getBlockZ() == afterCurve[track].getZ() && cart.getBlockX() <= afterCurve[track].getX(),
                        "cart " + track + " has not passed its curve: " + cart.getBlockPos());
                context.assertTrue(cart.isOnRail(), "cart " + track + " derailed at " + cart.getBlockPos());
            }
        });
    }

    @GameTest(templateName = EMPTY_STRUCTURE, batchId = "minecart_slope_up", tickLimit = 200)
    public void fullSpeedSlopeIsClimbed(TestContext context) {
        int z = 0;
        for (; z >= -12; z--) {
            poweredRail(context, at(X, 1, z), RailShape.NORTH_SOUTH);
        }
        // Three ascending rails, each one block higher, rising towards -z.
        int y = 1;
        for (int step = 0; step < 3; step++, z--, y++) {
            poweredRail(context, at(X, y, z), RailShape.ASCENDING_NORTH);
        }
        int topStart = z;
        for (; z >= topStart - 15; z--) {
            poweredRail(context, at(X, y, z), RailShape.NORTH_SOUTH);
        }
        context.setBlockState(at(X, y, z), Blocks.STONE);
        BlockPos top = context.getAbsolutePos(at(X, y, topStart - 3));
        MinecartEntity[] cart = launchLater(context, new Vec3d(X, 1, 0));
        context.addInstantFinalTask(() -> {
            context.assertTrue(cart[0] != null, "cart not launched yet");
            context.assertTrue(cart[0].getBlockY() == top.getY() && cart[0].getBlockZ() <= top.getZ(),
                    "cart has not reached the top yet: " + cart[0].getBlockPos());
            context.assertTrue(cart[0].isOnRail(), "cart derailed on the slope at " + cart[0].getBlockPos());
        });
    }

    @GameTest(templateName = EMPTY_STRUCTURE, batchId = "minecart_slope_down", tickLimit = 200)
    public void fullSpeedDescentStaysOnTheRails(TestContext context) {
        int y = 4;
        int z = 0;
        for (; z >= -10; z--) {
            poweredRail(context, at(X, y, z), RailShape.NORTH_SOUTH);
        }
        // Three descending rails towards -z (each rises towards +z, where the cart comes from).
        for (int step = 0; step < 3; step++, z--) {
            y--;
            poweredRail(context, at(X, y, z), RailShape.ASCENDING_SOUTH);
        }
        int bottomStart = z;
        for (; z >= bottomStart - 15; z--) {
            poweredRail(context, at(X, y, z), RailShape.NORTH_SOUTH);
        }
        context.setBlockState(at(X, y, z), Blocks.STONE);
        BlockPos bottom = context.getAbsolutePos(at(X, y, bottomStart - 3));
        MinecartEntity[] cart = launchLater(context, new Vec3d(X, 4, 0));
        context.addInstantFinalTask(() -> {
            context.assertTrue(cart[0] != null, "cart not launched yet");
            context.assertTrue(cart[0].getBlockY() == bottom.getY() && cart[0].getBlockZ() <= bottom.getZ(),
                    "cart has not reached the bottom yet: " + cart[0].getBlockPos());
            context.assertTrue(cart[0].isOnRail(), "cart derailed on the descent at " + cart[0].getBlockPos());
        });
    }

    /**
     * Force-loads the track area and launches a cart towards -z from the given relative block position (plus
     * any fractional offset) once the chunks are ticking. The returned holder is filled at {@link #LAUNCH_TICK}.
     */
    private static MinecartEntity[] launchLater(TestContext context, Vec3d relativeStart) {
        BlockPos startBlock = BlockPos.ofFloored(relativeStart.add(0.0, TRACK_Y, 0.0));
        Vec3d position = Vec3d.ofBottomCenter(context.getAbsolutePos(startBlock)).add(0.0, 0.0, relativeStart.z - startBlock.getZ());
        setForced(context, new ChunkPos(context.getAbsolutePos(at(X - 16, 1, -TRACK_LENGTH))),
                new ChunkPos(context.getAbsolutePos(at(X + 16 + 4 * CURVE_TRACKS, 1, 0))));
        MinecartEntity[] holder = new MinecartEntity[1];
        context.runAtTick(LAUNCH_TICK, () -> {
            MinecartEntity cart = EntityType.MINECART.create(context.getWorld());
            if (cart == null) {
                context.throwGameTestException("could not create a minecart");
                return;
            }
            cart.refreshPositionAndAngles(position.x, position.y, position.z, 0.0F, 0.0F);
            cart.setVelocity(0.0, 0.0, -1.5);
            context.getWorld().spawnEntity(cart);
            holder[0] = cart;
        });
        return holder;
    }

    /** A track position: relative x and z, and y relative to {@link #TRACK_Y}. */
    private static BlockPos at(int x, int y, int z) {
        return new BlockPos(x, TRACK_Y + y, z);
    }

    private static void setForced(TestContext context, ChunkPos a, ChunkPos b) {
        for (int x = Math.min(a.x, b.x); x <= Math.max(a.x, b.x); x++) {
            for (int z = Math.min(a.z, b.z); z <= Math.max(a.z, b.z); z++) {
                context.getWorld().setChunkForced(x, z, true);
            }
        }
    }

    private static void poweredRail(TestContext context, BlockPos pos, RailShape shape) {
        context.setBlockState(pos.down(), Blocks.REDSTONE_BLOCK);
        BlockState rail = Blocks.POWERED_RAIL.getDefaultState().with(PoweredRailBlock.SHAPE, shape).with(PoweredRailBlock.POWERED, true);
        context.setBlockState(pos, rail);
    }

    private static void rail(TestContext context, BlockPos pos, RailShape shape) {
        context.setBlockState(pos.down(), Blocks.STONE);
        context.setBlockState(pos, Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, shape));
    }
}
