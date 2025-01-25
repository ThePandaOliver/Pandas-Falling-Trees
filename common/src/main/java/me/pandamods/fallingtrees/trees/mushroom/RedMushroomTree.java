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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

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

		Set<BlockPos> stemBlocks = gatherStemBlocks(level, blockPos);
		Set<BlockPos> capBlocks = new HashSet<>();

		stemBlocks.forEach(stemPos -> capBlocks.addAll(gatherCapBlocks(level, stemPos.above())));
		if (capBlocks.isEmpty()) return null;

		List<BlockPos> blocks = new ArrayList<>();
		blocks.addAll(stemBlocks);
		blocks.addAll(capBlocks);

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
				.addAwardedStats(blocks.stream().map(logPos -> {
					BlockState blockState = level.getBlockState(logPos);
					return Stats.BLOCK_MINED.get(blockState.getBlock());
				}).toList())
				.build();
	}

	private Set<BlockPos> gatherStemBlocks(Level level, BlockPos startPos) {
		Set<BlockPos> blocks = new HashSet<>();
		Stack<BlockPos> toVisit = new Stack<>();
		Set<BlockPos> visited = new HashSet<>();

		toVisit.add(startPos);
		while (!toVisit.isEmpty()) {
			BlockPos current = toVisit.pop();
			if (visited.contains(current)) {
				continue;
			}
			visited.add(current);

			BlockState currentState = level.getBlockState(current);
			if (isTreeStem(currentState)) {
				blocks.add(current);

				BlockPos neighbor = current.above();
				if (!visited.contains(neighbor)) {
					toVisit.add(neighbor);
				}
			}
		}
		return blocks;
	}

	private Set<BlockPos> gatherCapBlocks(Level level, BlockPos startPos) {
		Set<BlockPos> blocks = new HashSet<>();
		Queue<BlockSearchNode> toVisit = new LinkedList<>();
		Set<BlockPos> visited = new HashSet<>();

		toVisit.add(new BlockSearchNode(startPos, 1));
		while (!toVisit.isEmpty()) {
			BlockSearchNode node = toVisit.poll();
			BlockPos current = node.position();

			if (visited.contains(current) || node.distance() > 6) {
				continue;
			}
			visited.add(current);

			BlockState currentState = level.getBlockState(current);
			if (currentState.is(Blocks.RED_MUSHROOM_BLOCK)) {
				blocks.add(current);

				for (BlockPos offset : CAP_SCAN_OFFSET) {
					BlockPos neighbor = current.offset(offset);
					if (!visited.contains(neighbor)) {
						toVisit.add(new BlockSearchNode(neighbor, node.distance() + 1));
					}
				}
			}
		}
		return blocks;
	}

	private record BlockSearchNode(BlockPos position, int distance) { }

	public TreeConfig getConfig() {
		return FallingTreesConfig.getCommonConfig().trees.mushroomTree;
	}
}
