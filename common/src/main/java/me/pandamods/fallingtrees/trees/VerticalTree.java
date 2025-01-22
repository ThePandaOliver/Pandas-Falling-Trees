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

import me.pandamods.fallingtrees.api.TreeData;
import me.pandamods.fallingtrees.api.TreeType;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.config.common.tree.VerticalTreeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class VerticalTree implements TreeType {
	@Override
	public boolean isTreeStem(BlockState blockState) {
		return getConfig().filter.isValid(blockState);
	}

	@Override
	public TreeData gatherTreeData(BlockPos blockPos, Level level) {
		blockPos = blockPos.immutable();
		TreeData.Builder builder = TreeData.builder();

		List<BlockPos> blocks = new ArrayList<>();
		gatherBlocks(level, blockPos, blocks);

		return builder
				.addBlocks(blocks)
				.setToolDamage(blocks.size())
				.setFoodExhaustionModifier(originalExhaustion -> originalExhaustion * blocks.size())
				.setMiningSpeedModifier(originalMiningSpeed -> {
					float speedMultiplication = FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.speedMultiplication;
					float multiplyAmount = Math.min(FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.maxSpeedMultiplication, ((float) blocks.size() - 1f));
					return originalMiningSpeed / (multiplyAmount * speedMultiplication + 1f);
				})
				.build();
	}

	private void gatherBlocks(Level level, BlockPos blockPos, List<BlockPos> blocks) {
		BlockState blockState = level.getBlockState(blockPos);
		blocks.add(blockPos);

		BlockPos neighborPos = blockPos.above();
		if (level.getBlockState(neighborPos).is(blockState.getBlock()))
			gatherBlocks(level, neighborPos, blocks);
	}

	public VerticalTreeConfig getConfig() {
		return FallingTreesConfig.getCommonConfig().trees.verticalTree;
	}
}
