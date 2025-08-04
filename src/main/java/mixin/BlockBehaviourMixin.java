/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU General Public License v3.0
 * See: https://www.gnu.org/licenses/gpl-3.0-standalone.html
 */

package dev.pandasystems.fallingtrees.mixin;

import dev.pandasystems.fallingtrees.api.TreeHandler;
import dev.pandasystems.fallingtrees.config.FallingTreesConfigKt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
	@Inject(method = "getDestroyProgress", at = @At("RETURN"), cancellable = true)
	private void getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		if (FallingTreesConfigKt.getFallingTreesCommonConfig().getConfig().getDynamicMiningSpeed().getDisable()) return;
		if (player == null || !TreeHandler.canPlayerChopTree(player)) return;
		Optional<Float> miningSpeedOpt = TreeHandler.getMiningSpeed(player, pos, cir.getReturnValue());
		miningSpeedOpt.ifPresent(cir::setReturnValue);
	}
}
