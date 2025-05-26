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
import me.pandamods.fallingtrees.config.ClientConfig;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.config.common.tree.GenericTreeConfig;
import me.pandamods.fallingtrees.entity.TreeEntity;
import me.pandamods.fallingtrees.exceptions.TreeTooBigException;
import me.pandamods.fallingtrees.registry.SoundRegistry;
import dev.pandasystems.pandalib.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
	public void onTreeTick(TreeEntity entity) {
		if (Services.GAME.isClient()) {
			ClientConfig clientConfig = FallingTreesConfig.getClientConfig();
			if (entity.tickCount == 1) {
				if (clientConfig.soundSettings.enabled) {
					entity.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.TREE_FALL.get(),
							SoundSource.BLOCKS, clientConfig.soundSettings.startVolume, 1f, true);
				}
			}

			if (entity.tickCount == (int) (clientConfig.animation.fallAnimLength * 20) - 5) {
				if (clientConfig.soundSettings.enabled) {
					entity.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.TREE_IMPACT.get(),
							SoundSource.BLOCKS, clientConfig.soundSettings.endVolume, 1f, true);
				}
			}
		}
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

		Set<BlockPos> allBlocks = new HashSet<>(logs);
		allBlocks.addAll(leaves);
		allBlocks.addAll(adjacent);

		List<ItemStack> drops = new ArrayList<>();
		for (BlockPos block : allBlocks) {
			BlockState blockState = level.getBlockState(block);
			if (level instanceof ServerLevel serverLevel) {
				List<ItemStack> items = Block.getDrops(blockState, serverLevel, block, null, player, player.getMainHandItem());
				drops.addAll(items);
			}
		}

		return builder
				.addBlocks(allBlocks)
				.setToolDamage(logs.size())
				.setFoodExhaustionModifier(originalExhaustion -> originalExhaustion * logs.size())
				.addDrops(drops)
				.setMiningSpeedModifier(originalMiningSpeed -> {
					float speedMultiplication = FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.speedMultiplication;
					float multiplyAmount = Math.min(FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.maxSpeedMultiplication, ((float) logs.size() - 1f));
					return originalMiningSpeed / (multiplyAmount * speedMultiplication + 1f);
				})
				.addAwardedStats(logs.stream().map(logPos -> {
					BlockState blockState = level.getBlockState(logPos);
					return Stats.BLOCK_MINED.get(blockState.getBlock());
				}).toList())
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
				BlockState neighborState = level.getBlockState(neighbor);
				if (neighborState.is(Blocks.VINE)) {
					adjacentBlocks.addAll(gatherVines(level, neighbor));
				} else if (neighborState.is(Blocks.BEE_NEST)) {
					adjacentBlocks.add(neighbor);
				} else if (neighborState.is(Blocks.COCOA)) {
					adjacentBlocks.add(neighbor);
				}
			}
		}
		return adjacentBlocks;
	}

	private Set<BlockPos> gatherVines(Level level, BlockPos startPos) {
		Set<BlockPos> vines = new HashSet<>();
		Stack<BlockPos> toVisit = new Stack<>();
		Set<BlockPos> visited = new HashSet<>();

		toVisit.push(startPos);
		while (!toVisit.isEmpty()) {
			BlockPos current = toVisit.pop();
			if (visited.contains(current)) {
				continue;
			}
			visited.add(current);

			BlockState currentState = level.getBlockState(current);
			if (currentState.is(Blocks.VINE)) {
				vines.add(current);

				BlockPos neighbor = current.below();
				if (!visited.contains(neighbor)) {
					toVisit.push(neighbor);
				}
			}
		}
		return vines;
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

	private record BlockSearchNode(BlockPos position, int distance) { }

	public GenericTreeConfig getConfig() {
		return FallingTreesConfig.getCommonConfig().trees.genericTree;
	}
}
