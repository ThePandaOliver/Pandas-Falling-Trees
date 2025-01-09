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

package me.pandamods.fallingtrees.trees;

import me.pandamods.fallingtrees.api.Tree;
import me.pandamods.fallingtrees.api.TreeData;
import me.pandamods.fallingtrees.api.TreeDataBuilder;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.config.common.tree.VerticalTreeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class VerticalTree implements Tree<VerticalTreeConfig> {
	@Override
	public boolean isTreeStem(BlockState blockState) {
		return getConfig().filter.isValid(blockState);
	}

	@Override
	public TreeData getTreeData(TreeDataBuilder builder, BlockPos blockPos, BlockGetter level) {
		loopBlocks(level.getBlockState(blockPos), blockPos, level, builder);
		return builder
				.setAwardedBlocks(builder.getBlocks().size())
				.setFoodExhaustion(builder.getBlocks().size())
				.setToolDamage(builder.getBlocks().size())
				.build(true);
	}

	private void loopBlocks(BlockState blockState, BlockPos blockpos, BlockGetter level, TreeDataBuilder builder) {
		builder.addBlock(blockpos);
		if (blockState.is(level.getBlockState(blockpos.above()).getBlock())) {
			loopBlocks(blockState, blockpos.above(), level, builder);
		}
	}

	@Override
	public float fallAnimationEdgeDistance() {
		return 0;
	}

	@Override
	public VerticalTreeConfig getConfig() {
		return FallingTreesConfig.getCommonConfig().trees.verticalTree;
	}

	@Override
	public boolean enabled() {
		return getConfig().enabled;
	}
}
