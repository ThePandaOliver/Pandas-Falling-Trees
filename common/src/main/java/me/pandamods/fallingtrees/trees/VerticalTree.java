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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class VerticalTree implements TreeType {
	@Override
	public boolean isTreeStem(BlockState blockState) {
		return getConfig().filter.isValid(blockState);
	}

	@Override
	public TreeData gatherTreeData(BlockPos blockPos, Level level, Player player) {
		if (getConfig().requireTool && !getConfig().allowedToolFilter.isValid(player.getMainHandItem())) return null;

		blockPos = blockPos.immutable();
		TreeData.Builder builder = TreeData.builder();

		List<BlockPos> blocks = new ArrayList<>();
		gatherBlocks(level, blockPos, blocks);

		List<ItemStack> drops = new ArrayList<>();
		if (level instanceof ServerLevel serverLevel) {
			for (BlockPos block : blocks) {
				BlockState blockState = level.getBlockState(block);
				List<ItemStack> items = Block.getDrops(blockState, serverLevel, block, null, player, player.getMainHandItem());
				drops.addAll(items);
			}
		}

		return builder
				.addBlocks(blocks)
				.setToolDamage(blocks.size())
				.setFoodExhaustionModifier(originalExhaustion -> originalExhaustion * blocks.size())
				.addDrops(drops)
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
