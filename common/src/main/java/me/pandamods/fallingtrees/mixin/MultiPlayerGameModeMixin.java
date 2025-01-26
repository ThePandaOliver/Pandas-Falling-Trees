/*
 * Copyright (C) 2024 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.pandamods.fallingtrees.mixin;

import me.pandamods.fallingtrees.compat.Compat;
import me.pandamods.fallingtrees.compat.TreeChopCompat;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.registry.TreeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Shadow private BlockPos destroyBlockPos;

	@Shadow public abstract boolean isDestroying();

	@Shadow @Final private Minecraft minecraft;
	@Unique
	private boolean fallingTrees$lastTickCrouchState = false;
	@Unique
	private Direction fallingTrees$blockDestroyDirection = Direction.UP;

	@Inject(method = "startDestroyBlock", at = @At("RETURN"))
	public void startDestroyBlock(BlockPos loc, Direction face, CallbackInfoReturnable<Boolean> cir) {
		fallingTrees$blockDestroyDirection = face;
	}

	@Inject(method = "tick", at = @At("RETURN"))
	public void tick(CallbackInfo ci) {
		if (FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.disable || Compat.hasTreeChop()) return;
		Player player = minecraft.player;

		if (player != null) {
			Level level = player.level();

			BlockState blockState = level.getBlockState(this.destroyBlockPos);
			if (TreeRegistry.getTree(blockState) != null) {
				if (player.isCrouching() != fallingTrees$lastTickCrouchState) {
					if (this.isDestroying() && minecraft.gameMode != null) {
						MultiPlayerGameMode gameMode = minecraft.gameMode;
						gameMode.stopDestroyBlock();
						gameMode.startDestroyBlock(this.destroyBlockPos, fallingTrees$blockDestroyDirection);
					}
				}
				this.fallingTrees$lastTickCrouchState = player.isCrouching();
			}
		}
	}
}
