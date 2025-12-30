/*
 * Copyright (C) 2025-2025 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.pandasystems.fallingtrees.neoforge.mixin;

import dev.pandasystems.fallingtrees.api.TreeHandler;
import dev.pandasystems.fallingtrees.config.FallingTreesConfigKt;
import dev.pandasystems.fallingtrees.mixin.BlockBehaviourKtImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
	@Unique
	private BlockBehaviourKtImpl fallingtrees$impl =  new BlockBehaviourKtImpl();

	@Inject(method = "getDestroyProgress", at = @At("RETURN"), cancellable = true)
	private void getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		fallingtrees$impl.getDestroyProgress(player, pos, cir);
	}
}
