package com.iwaca.progressionrebalance.mixin;

import com.iwaca.progressionrebalance.transport.MinecartSpeed;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Why: minecart speed on rails is limited by a single clamp in {@code moveOnRail}. Only that call site is
 * changed; {@code getMaxSpeed()} itself (also used for off-rail movement, where faster carts would fly
 * further after derailing) keeps its vanilla value. The rail's shape is passed along so curves can keep the
 * vanilla clamp (see {@link MinecartSpeed}). Movement is server-side only in 1.21.1; clients just
 * interpolate positions, so no client change is needed.
 */
@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin {
    @ModifyExpressionValue(method = "moveOnRail", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;getMaxSpeed()D"))
    private double progressionrebalance$fasterOnRails(double vanillaMaxSpeed, @Local(argsOnly = true) BlockState state) {
        return MinecartSpeed.onRailMaxSpeed(vanillaMaxSpeed, state.get(((AbstractRailBlock) state.getBlock()).getShapeProperty()));
    }
}
