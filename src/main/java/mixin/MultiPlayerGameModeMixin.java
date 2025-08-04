/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU General Public License v3.0
 * See: https://www.gnu.org/licenses/gpl-3.0-standalone.html
 */

package dev.pandasystems.fallingtrees.mixin;

import dev.pandasystems.fallingtrees.FallingTreesRegistriesKt;
import dev.pandasystems.fallingtrees.config.FallingTreesConfigKt;
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
		if (FallingTreesConfigKt.getFallingTreesCommonConfig().getConfig().getDynamicMiningSpeed().getDisable()) return;
		Player player = minecraft.player;

		if (player != null) {
			Level level = player.level();

			BlockState blockState = level.getBlockState(this.destroyBlockPos);
			if (FallingTreesRegistriesKt.getTree(blockState) != null) {
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
