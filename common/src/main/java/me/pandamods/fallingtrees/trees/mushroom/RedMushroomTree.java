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

package me.pandamods.fallingtrees.trees.mushroom;

import me.pandamods.fallingtrees.api.TreeData;
import me.pandamods.fallingtrees.api.TreeType;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.config.common.tree.TreeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class RedMushroomTree implements TreeType {
	private static final BlockPos[] CAP_SCAN_OFFSET = new BlockPos[] {
			new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0),
			new BlockPos(0, 0, -1), new BlockPos(0, 0, 1),
			new BlockPos(-1, -1, 0), new BlockPos(1, -1, 0),
			new BlockPos(0, -1, -1), new BlockPos(0, -1, 1)
	};

	@Override
	public boolean isTreeStem(BlockState blockState) {
		return blockState.is(Blocks.MUSHROOM_STEM);
	}

	@Override
	public TreeData gatherTreeData(BlockPos blockPos, Level level, Player player) {
		if (getConfig().requireTool && !getConfig().allowedToolFilter.isValid(player.getMainHandItem())) return null;

		blockPos = blockPos.immutable();
		TreeData.Builder builder = TreeData.builder();

		List<BlockPos> stemBlocks = new ArrayList<>();
		List<BlockPos> capBlocks = new ArrayList<>();

		List<BlockPos> checkedStemBlocks = new ArrayList<>();
		List<BlockPos> checkedCapBlocks = new ArrayList<>();

		gatherStemBlocks(level, blockPos, stemBlocks, checkedStemBlocks);
		stemBlocks.forEach(stemPos -> gatherCapBlocks(level, stemPos.above(), capBlocks, checkedCapBlocks, 0));
		if (capBlocks.isEmpty()) return null;

		List<BlockPos> blocks = new ArrayList<>();
		blocks.addAll(stemBlocks);
		blocks.addAll(capBlocks);

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

	private void gatherStemBlocks(Level level, BlockPos blockPos, List<BlockPos> blocks, List<BlockPos> checkedBlocks) {
		if (checkedBlocks.contains(blockPos)) return;
		checkedBlocks.add(blockPos);

		BlockState blockState = level.getBlockState(blockPos);
		if (this.isTreeStem(blockState)) {
			blocks.add(blockPos);

			BlockPos neighborPos = blockPos.above();
			gatherStemBlocks(level, neighborPos, blocks, checkedBlocks);
		}
	}

	private void gatherCapBlocks(Level level, BlockPos blockPos, List<BlockPos> blocks, List<BlockPos> checkedBlocks, int scanRadius) {
		if (scanRadius >= 6 || checkedBlocks.contains(blockPos)) return;
		checkedBlocks.add(blockPos);

		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.is(Blocks.RED_MUSHROOM_BLOCK)) {
			blocks.add(blockPos);

			for (BlockPos offset : CAP_SCAN_OFFSET) {
				BlockPos neighborPos = blockPos.offset(offset);
				gatherCapBlocks(level, neighborPos, blocks, checkedBlocks, scanRadius + 1);
			}
		}
	}

	public TreeConfig getConfig() {
		return FallingTreesConfig.getCommonConfig().trees.mushroomTree;
	}
}
