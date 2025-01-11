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
import me.pandamods.fallingtrees.config.common.tree.GenericTreeConfig;
import me.pandamods.fallingtrees.exceptions.TreeTooBigException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.*;

public class GenericTree implements TreeType {
	@Override
	public boolean isTreeStem(BlockState blockState) {
		return getConfig().logFilter.isValid(blockState);
	}

	public boolean isValidLeavesBlock(BlockState blockState) {
		if (getConfig().algorithm.shouldIgnorePersistentLeaves &&
				blockState.hasProperty(BlockStateProperties.PERSISTENT) && blockState.getValue(BlockStateProperties.PERSISTENT))
			return false;
		return getConfig().leavesFilter.isValid(blockState);
	}

	@Override
	public TreeData gatherTreeData(BlockPos blockPos, Level level) {
		blockPos = blockPos.immutable();
		TreeData.Builder builder = TreeData.builder();
		
		List<BlockPos> logBlocks = new ArrayList<>();
		List<BlockPos> leavesBlocks = new ArrayList<>();
		List<BlockPos> adjacentBlocks = new ArrayList<>();

		List<BlockPos> checkedLogBlocks = new ArrayList<>();
		List<BlockPos> checkedLeavesBlocks = new ArrayList<>();
		List<BlockPos> checkedAdjacentBlocks = new ArrayList<>();

		gatherLogs(level, blockPos, logBlocks, checkedLogBlocks, blockPos);

		logBlocks.forEach(logPos -> {
			for (Direction direction : Direction.values()) {
				BlockPos neighborPos = logPos.relative(direction);
				gatherLeaves(level, neighborPos, leavesBlocks, checkedLeavesBlocks, 1);
			}
		});
		if (leavesBlocks.isEmpty()) return null;

		List<BlockPos> treeBlocks = new ArrayList<>();
		treeBlocks.addAll(logBlocks);
		treeBlocks.addAll(leavesBlocks);

		treeBlocks.forEach(pos -> {
			for (Direction direction : Direction.values()) {
				BlockPos neighborPos = pos.relative(direction);
				gatherAdjacentBlocks(level, neighborPos, adjacentBlocks, checkedAdjacentBlocks);
			}
		});

		return builder
				.addBlocks(logBlocks)
				.addBlocks(leavesBlocks)
				.addBlocks(adjacentBlocks)
				.setAwardedBlocks(logBlocks.size())
				.setFoodExhaustion(logBlocks.size())
				.setToolDamage(logBlocks.size())
				.setMiningSpeedModifier((blockState, originalMiningSpeed) -> {
					float speedMultiplication = FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.speedMultiplication;
					float multiplyAmount = Math.min(FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.maxSpeedMultiplication, ((float) logBlocks.size() - 1f));
					return originalMiningSpeed / (multiplyAmount * speedMultiplication + 1f);
				})
				.build();
	}

	private void gatherLogs(Level level, BlockPos blockPos, List<BlockPos> blocks, List<BlockPos> checkedBlocks, BlockPos originBlocks) {
		if (checkedBlocks.contains(blockPos)) return;
		checkedBlocks.add(blockPos);

		BlockState blockState = level.getBlockState(blockPos);
		if (this.isTreeStem(blockState)) {
			blocks.add(blockPos);
			if (blocks.size() >= getConfig().algorithm.maxLogAmount)
				throw new TreeTooBigException(originBlocks, level);
			
			for (BlockPos offset : BlockPos.betweenClosed(-1, 0, -1, 1, 1, 1)) {
				BlockPos neighborPos = blockPos.offset(offset);
				gatherLogs(level, neighborPos, blocks, checkedBlocks, originBlocks);
			}
		}
	}

	private void gatherLeaves(Level level, BlockPos blockPos, List<BlockPos> blocks, List<BlockPos> checkedBlocks, int recursionDistance) {
		if (recursionDistance > getConfig().algorithm.maxLeavesRadius) return;
		
		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.hasProperty(LeavesBlock.DISTANCE) && blockState.getValue(LeavesBlock.DISTANCE) != recursionDistance) return;
		if (checkedBlocks.contains(blockPos)) return;
		checkedBlocks.add(blockPos);

		if (this.isValidLeavesBlock(blockState)) {
			blocks.add(blockPos);

			for (Direction direction : Direction.values()) {
				BlockPos neighborPos = blockPos.relative(direction);
				gatherLeaves(level, neighborPos, blocks, checkedBlocks, recursionDistance + 1);
			}
		}
	}

	private void gatherAdjacentBlocks(BlockGetter level, BlockPos blockPos, List<BlockPos> blocks, List<BlockPos> checkedBlocks) {
		if (checkedBlocks.contains(blockPos)) return;
		checkedBlocks.add(blockPos);

		BlockState blockState = level.getBlockState(blockPos);
		if (getConfig().adjacentBlockFilter.isValid(blockState)) {
			blocks.add(blockPos);

			gatherAdjacentBlocks(level, blockPos.offset(Direction.DOWN.getUnitVec3i()), blocks, checkedBlocks);
		}
	}

	public GenericTreeConfig getConfig() {
		return FallingTreesConfig.getCommonConfig().trees.genericTree;
	}
}
