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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

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

		List<BlockPos> blocks = new ArrayList<>();
		List<BlockPos> checkedBlocks = new ArrayList<>();
		gatherBlocks(level, blockPos, blocks, checkedBlocks, builder, player);

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

	private void gatherBlocks(Level level, BlockPos blockPos, List<BlockPos> blocks, List<BlockPos> checkedBlocks, TreeData.Builder builder, Player player) {
		if (checkedBlocks.contains(blockPos)) return;
		checkedBlocks.add(blockPos);

		BlockState blockState = level.getBlockState(blockPos);
		if (isFlower(blockState)) {
			blocks.add(blockPos);
			return;
		}

		if (isPlant(blockState)) {
			blocks.add(blockPos);
			builder.addAwardedStat(Stats.BLOCK_MINED.get(blockState.getBlock()));
			if (level instanceof ServerLevel serverLevel)
				builder.addDrops(Block.getDrops(blockState, serverLevel, blockPos, null, player, player.getMainHandItem()));

			for (BlockPos neighborPos : gatherValidBlocksAround(level, blockPos)) {
				gatherBlocks(level, neighborPos, blocks, checkedBlocks, builder, player);
			}
		}
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
