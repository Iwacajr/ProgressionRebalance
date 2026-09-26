package com.iwaca.progressionrebalance.transport;

import com.iwaca.progressionrebalance.config.ConfigManager;
import net.minecraft.block.enums.RailShape;

/**
 * Minecart top speed on rails.
 *
 * <p>Vanilla clamps the distance a cart moves each tick to {@code getMaxSpeed()} (0.4 blocks per tick,
 * 8 blocks/s, halved in water), separately on the x and z axes. Raising only that clamp is the smallest change
 * that makes carts faster: acceleration from powered rails, friction and slopes stay vanilla.
 *
 * <p>Curves keep the vanilla clamp. On a curve the cart moves diagonally, up to the clamp on <em>both</em>
 * axes, and anything above half a block per axis can carry it past the curve into the diagonal neighbour,
 * where there is no rail. The cart keeps its momentum through the curve and is back at full speed on the
 * next straight block. Straight rails are safe up to one block per tick (the config maximum): a cart then
 * still stops in every rail block it passes, so it cannot skip a curve.
 */
public final class MinecartSpeed {
    private MinecartSpeed() {
    }

    public static double onRailMaxSpeed(double vanillaMaxSpeed, RailShape shape) {
        if (isCurve(shape)) {
            return vanillaMaxSpeed;
        }
        return vanillaMaxSpeed * ConfigManager.get().transport().minecartSpeedMultiplier();
    }

    static boolean isCurve(RailShape shape) {
        return switch (shape) {
            case NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST -> true;
            default -> false;
        };
    }
}
