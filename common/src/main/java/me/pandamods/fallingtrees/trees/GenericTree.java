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

import me.pandamods.fallingtrees.FallingTrees;
import me.pandamods.fallingtrees.api.TreeData;
import me.pandamods.fallingtrees.api.TreeType;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.config.common.tree.GenericTreeConfig;
import me.pandamods.fallingtrees.exceptions.TreeTooBigException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
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

	@Override
	public TreeData gatherTreeData(BlockPos blockPos, Level level, Player player) {
		if (getConfig().requireTool && !getConfig().allowedToolFilter.isValid(player.getMainHandItem())) return null;
		
		blockPos = blockPos.immutable();
		TreeData.Builder builder = TreeData.builder();

		if (!isLogBlock(level.getBlockState(blockPos))) {
			return null;
		}

		Set<BlockPos> logs = gatherLogs(level, blockPos);
		if (logs.isEmpty()) {
			return null;
		}

		Set<BlockPos> leaves = new HashSet<>();
		for (BlockPos logPos : logs) {
			leaves.addAll(gatherLeavesAroundLog(level, logPos));
		}
		if (leaves.isEmpty()) {
			return null;
		}

		Set<BlockPos> adjacent = gatherAdjacentBlocks(level, logs, leaves);

		return builder
				.addBlocks(logs)
				.addBlocks(leaves)
				.addBlocks(adjacent)
				.setToolDamage(logs.size())
				.setFoodExhaustionModifier(originalExhaustion -> originalExhaustion * logs.size())
				.setMiningSpeedModifier(originalMiningSpeed -> {
					float speedMultiplication = FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.speedMultiplication;
					float multiplyAmount = Math.min(FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.maxSpeedMultiplication, ((float) logs.size() - 1f));
					return originalMiningSpeed / (multiplyAmount * speedMultiplication + 1f);
				})
				.build();
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

			BlockState currentState = level.getBlockState(current);
			OptionalInt optionalDistanceAt = LeavesBlock.getOptionalDistanceAt(currentState);
			if (node.distance != optionalDistanceAt.orElse(0)) {
				continue;
			}
			if (visited.contains(current) || node.distance > getConfig().algorithm.maxLeavesRadius) {
				continue;
			}
			visited.add(current);

			if (isLeafBlock(currentState)) {
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

	private boolean isLogBlock(BlockState blockState) {
		return getConfig().logFilter.isValid(blockState);
	}

	private boolean isLeafBlock(BlockState blockState) {
		if (getConfig().algorithm.shouldIgnorePersistentLeaves &&
				blockState.hasProperty(BlockStateProperties.PERSISTENT) && blockState.getValue(BlockStateProperties.PERSISTENT))
			return false;
		return getConfig().leavesFilter.isValid(blockState);
	}

	private boolean isValidAdjacent(BlockState blockState) {
		return getConfig().adjacentBlockFilter.isValid(blockState);
	}

	private record BlockSearchNode(BlockPos position, int distance) { }

	public GenericTreeConfig getConfig() {
		return FallingTreesConfig.getCommonConfig().trees.genericTree;
	}
}
