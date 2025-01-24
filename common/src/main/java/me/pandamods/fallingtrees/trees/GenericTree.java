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
import me.pandamods.fallingtrees.api.TreeDetectionAlgorithm;
import me.pandamods.fallingtrees.api.TreeType;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.config.common.tree.GenericTreeConfig;
import me.pandamods.fallingtrees.exceptions.TreeTooBigException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
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
	public TreeData gatherTreeData(BlockPos blockPos, Level level, Player player) {
		if (getConfig().requireTool && !getConfig().allowedToolFilter.isValid(player.getMainHandItem())) return null;
		
		blockPos = blockPos.immutable();
		TreeData.Builder builder = TreeData.builder();
		
//		List<BlockPos> logBlocks = new ArrayList<>();
//		List<BlockPos> leavesBlocks = new ArrayList<>();
//		List<BlockPos> adjacentBlocks = new ArrayList<>();
//
//		List<BlockPos> checkedLogBlocks = new ArrayList<>();
//		List<BlockPos> checkedLeavesBlocks = new ArrayList<>();
//		List<BlockPos> checkedAdjacentBlocks = new ArrayList<>();
//
//		gatherLogs(level, blockPos, logBlocks, checkedLogBlocks, blockPos);
//
//		logBlocks.forEach(logPos -> {
//			for (Direction direction : Direction.values()) {
//				BlockPos neighborPos = logPos.relative(direction);
//				gatherLeaves(level, neighborPos, leavesBlocks, checkedLeavesBlocks, 1);
//			}
//		});
//		if (leavesBlocks.isEmpty()) return null;
//
//		List<BlockPos> treeBlocks = new ArrayList<>();
//		treeBlocks.addAll(logBlocks);
//		treeBlocks.addAll(leavesBlocks);
//
//		treeBlocks.forEach(pos -> {
//			for (Direction direction : Direction.values()) {
//				BlockPos neighborPos = pos.relative(direction);
//				gatherAdjacentBlocks(level, neighborPos, adjacentBlocks, checkedAdjacentBlocks);
//			}
//		});

		TreeDetectionAlgorithm algorithm = new Algorithm();
		Set<BlockPos> blockPosSet = algorithm.gatherTreeBlocks(level, blockPos);

		return builder
				.addBlocks(blockPosSet)
//				.addBlocks(logBlocks)
//				.addBlocks(leavesBlocks)
//				.addBlocks(adjacentBlocks)
//				.setToolDamage(logBlocks.size())
//				.setFoodExhaustionModifier(originalExhaustion -> originalExhaustion * logBlocks.size())
//				.setMiningSpeedModifier(originalMiningSpeed -> {
//					float speedMultiplication = FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.speedMultiplication;
//					float multiplyAmount = Math.min(FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.maxSpeedMultiplication, ((float) logBlocks.size() - 1f));
//					return originalMiningSpeed / (multiplyAmount * speedMultiplication + 1f);
//				})
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

	public class Algorithm extends TreeDetectionAlgorithm {
		@Override
		public Set<BlockPos> gatherTreeBlocks(Level level, BlockPos startPos) {
			if (!isLogBlock(level.getBlockState(startPos))) {
				return Collections.emptySet();
			}

			Set<BlockPos> logs = gatherLogs(level, startPos);
			if (logs.isEmpty()) {
				return Collections.emptySet();
			}

			Set<BlockPos> leaves = new HashSet<>();
			for (BlockPos logPos : logs) {
				leaves.addAll(gatherLeavesAroundLog(level, logPos));
			}
			if (leaves.isEmpty()) {
				return Collections.emptySet();
			}

			Set<BlockPos> adjacent = gatherAdjacentBlocks(level, logs, leaves);
			Set<BlockPos> allBlocks = new HashSet<>(logs);
			allBlocks.addAll(leaves);
			allBlocks.addAll(adjacent);

			return allBlocks;
		}

		private Set<BlockPos> gatherLogs(Level level, BlockPos startPos) {
			Set<BlockPos> logs = new HashSet<>();
			Queue<BlockPos> toVisit = new LinkedList<>();
			Set<BlockPos> visited = new HashSet<>();

			toVisit.add(startPos);

			while (!toVisit.isEmpty()) {
				BlockPos current = toVisit.poll();
				if (visited.contains(current)) {
					continue;
				}
				visited.add(current);

				BlockState currentState = level.getBlockState(current);
				if (isLogBlock(currentState)) {
					logs.add(current);

					if (logs.size() > getConfig().algorithm.maxLogAmount) {
						throw new TreeTooBigException(current, level);
					}

					for (BlockPos offset : BlockPos.betweenClosed(-1, 0, -1, 1, 1, 1)) {
						BlockPos neighbor = current.offset(offset);
						if (!visited.contains(neighbor)) {
							toVisit.add(neighbor);
						}
					}
				}
			}
			return logs;
		}

		private Set<BlockPos> gatherLeavesAroundLog(Level level, BlockPos logPos) {
			Set<BlockPos> leaves = new HashSet<>();
			Queue<BlockSearchNode> toVisit = new LinkedList<>();
			Set<BlockPos> visited = new HashSet<>();

			for (Direction direction : Direction.values()) {
				BlockPos neighbor = logPos.relative(direction);
				toVisit.add(new BlockSearchNode(neighbor, 1));
			}

			while (!toVisit.isEmpty()) {
				BlockSearchNode node = toVisit.poll();
				BlockPos current = node.position;

				if (visited.contains(current) || node.distance > 7) {
					continue;
				}
				visited.add(current);

				BlockState state = level.getBlockState(current);
				if (isLeafBlock(state)) {
					leaves.add(current);

					for (Direction direction : Direction.values()) {
						BlockPos nextPos = current.relative(direction);
						if (!visited.contains(nextPos)) {
							toVisit.add(new BlockSearchNode(nextPos, node.distance + 1));
						}
					}
				}
			}

			return leaves;
		}

		private Set<BlockPos> gatherAdjacentBlocks(Level level, Set<BlockPos> logs, Set<BlockPos> leaves) {
			Set<BlockPos> adjacentBlocks = new HashSet<>();
			Set<BlockPos> allTreeBlocks = new HashSet<>(logs);
			allTreeBlocks.addAll(leaves);

			// Example BFS or direct neighbor check:
			for (BlockPos blockPos : allTreeBlocks) {
				for (Direction dir : Direction.values()) {
					BlockPos neighbor = blockPos.relative(dir);
					if (isValidAdjacent(level.getBlockState(neighbor))) {
						adjacentBlocks.add(neighbor);
					}
				}
			}
			return adjacentBlocks;
		}

		private boolean isLogBlock(BlockState state) {
			return getConfig().logFilter.isValid(state);
		}

		private boolean isLeafBlock(BlockState state) {
			return getConfig().leavesFilter.isValid(state);
		}

		private boolean isValidAdjacent(BlockState state) {
			return getConfig().adjacentBlockFilter.isValid(state);
		}

		private static class BlockSearchNode {
			final BlockPos position;
			final int distance;

			BlockSearchNode(BlockPos position, int distance) {
				this.position = position;
				this.distance = distance;
			}
		}
	}
}
