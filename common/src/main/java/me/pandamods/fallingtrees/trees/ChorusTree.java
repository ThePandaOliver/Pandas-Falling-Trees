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
import me.pandamods.fallingtrees.config.common.tree.TreeConfig;
import me.pandamods.fallingtrees.exceptions.TreeTooBigException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class ChorusTree implements TreeType {
	private static final Direction[] HORIZONTAL_DIRECTIONS = new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

	@Override
	public boolean isTreeStem(BlockState blockState) {
		return isPlant(blockState);
	}

	private static boolean isPlant(BlockState blockState) {
		return blockState.is(Blocks.CHORUS_PLANT);
	}

	private static boolean isFlower(BlockState blockState) {
		return blockState.is(Blocks.CHORUS_FLOWER);
	}

	@Override
	public TreeData gatherTreeData(BlockPos blockPos, Level level, Player player) {
		if (getConfig().requireTool && !getConfig().allowedToolFilter.isValid(player.getMainHandItem())) return null;

		blockPos = blockPos.immutable();
		TreeData.Builder builder = TreeData.builder();

		Set<BlockPos> blockPosSet = gatherBlocks(level, blockPos, builder, player);
		return builder
				.addBlocks(blockPosSet)
				.setToolDamage(blockPosSet.size())
				.setFoodExhaustionModifier(originalExhaustion -> originalExhaustion * blockPosSet.size())
				.setMiningSpeedModifier(originalMiningSpeed -> {
					float speedMultiplication = FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.speedMultiplication;
					float multiplyAmount = Math.min(FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.maxSpeedMultiplication, ((float) blockPosSet.size() - 1f));
					return originalMiningSpeed / (multiplyAmount * speedMultiplication + 1f);
				})
				.build();
	}

	private Set<BlockPos> gatherBlocks(Level level, BlockPos startPos, TreeData.Builder builder, Player player) {
		Set<BlockPos> blocks = new HashSet<>();
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
			if (isFlower(currentState)) {
				blocks.add(current);
				continue;
			}

			if (isPlant(currentState)) {
				blocks.add(current);
				builder.addAwardedStat(Stats.BLOCK_MINED.get(currentState.getBlock()));

				if (level instanceof ServerLevel serverLevel)
					builder.addDrops(Block.getDrops(currentState, serverLevel, current, null, player, player.getMainHandItem()));

				for (BlockPos neighbor : gatherValidBlocksAround(level, current)) {
					if (!visited.contains(neighbor)) {
						toVisit.add(neighbor);
					}
				}
			}
		}
		return blocks;
	}

	private static List<BlockPos> gatherValidBlocksAround(Level level, BlockPos blockPos) {
		List<BlockPos> blocks = new ArrayList<>();
		for (Direction direction : HORIZONTAL_DIRECTIONS) {
			BlockPos neighborPos = blockPos.relative(direction);
			if (isPlant(level.getBlockState(neighborPos.below())))
				continue;
			BlockState blockState = level.getBlockState(neighborPos);
			if (isPlant(blockState) || isFlower(blockState))
				blocks.add(neighborPos);
		}
		BlockPos neighborPos = blockPos.above();
		BlockState blockState = level.getBlockState(neighborPos);
		if (isPlant(blockState) || isFlower(blockState))
			blocks.add(neighborPos);
		return blocks;
	}

	public TreeConfig getConfig() {
		return FallingTreesConfig.getCommonConfig().trees.chorusTree;
	}
}
