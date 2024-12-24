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

import me.pandamods.fallingtrees.api.Tree;
import me.pandamods.fallingtrees.api.TreeData;
import me.pandamods.fallingtrees.api.TreeDataBuilder;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.registry.TreeRegistry;
import me.pandamods.fallingtrees.utils.TreeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateMixin {
	@Inject(method = "getDestroyProgress", at = @At("RETURN"), cancellable = true)
	public void getDestroyProgress(Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		if (FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.disable) return;
		Tree<?> tree = TreeRegistry.getTree(level.getBlockState(pos));
		if (tree != null && tree.willTreeFall(pos, level, player)) {
			TreeData treeData = TreeCache.get(player, pos, () -> tree.getTreeData(new TreeDataBuilder(), pos, level));
			cir.setReturnValue(cir.getReturnValueF() * treeData.miningSpeedMultiply());
		}
	}
}
