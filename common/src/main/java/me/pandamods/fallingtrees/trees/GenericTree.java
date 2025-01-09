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

import dev.architectury.platform.Platform;
import me.pandamods.fallingtrees.api.Tree;
import me.pandamods.fallingtrees.api.TreeData;
import me.pandamods.fallingtrees.config.ClientConfig;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.config.common.tree.GenericTreeConfig;
import me.pandamods.fallingtrees.entity.TreeEntity;
import me.pandamods.fallingtrees.exceptions.TreeException;
import me.pandamods.fallingtrees.exceptions.TreeTooBigException;
import me.pandamods.fallingtrees.registry.SoundRegistry;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashSet;
import java.util.Set;

public class GenericTree implements Tree<GenericTreeConfig> {
	@Override
	public boolean isTreeStem(BlockState blockState) {
		return getConfig().logFilter.isValid(blockState);
	}

	public boolean extraRequiredBlockCheck(BlockState blockState) {
		if (getConfig().algorithm.shouldIgnorePersistentLeaves &&
				blockState.hasProperty(BlockStateProperties.PERSISTENT) && blockState.getValue(BlockStateProperties.PERSISTENT))
			return false;
		return getConfig().leavesFilter.isValid(blockState);
	}

	@Override
	public void entityTick(TreeEntity entity) {
		Tree.super.entityTick(entity);
		Level level = entity.level();

		if (Platform.getEnv() == EnvType.CLIENT) {
			ClientConfig clientConfig = FallingTreesConfig.getClientConfig();
			if (entity.tickCount == 1) {
				if (clientConfig.soundSettings.enabled) {
					level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.TREE_FALL.get(),
							SoundSource.BLOCKS, clientConfig.soundSettings.startVolume, 1f, true);
				}
			}

			if (entity.tickCount == (int) (clientConfig.animation.fallAnimLength * 20) - 5) {
				if (clientConfig.soundSettings.enabled) {
					level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.TREE_IMPACT.get(),
							SoundSource.BLOCKS, clientConfig.soundSettings.endVolume, 1f, true);
				}
			}
		}
	}

	@Override
	public TreeData gatherTreeData(BlockPos blockPos, Level level) throws TreeException {
		TreeData.Builder builder = TreeData.builder();
		
		Set<BlockPos> logBlocks = new HashSet<>();
		Set<BlockPos> leavesBlocks = new HashSet<>();
		Set<BlockPos> decorationBlocks = new HashSet<>();

		Set<BlockPos> loopedLogBlocks = new HashSet<>();
		Set<BlockPos> loopedDecorationBlocks = new HashSet<>();

		loopLogs(level, blockPos, logBlocks, loopedLogBlocks, blockPos);

		logBlocks.forEach(logPos -> {
			Set<BlockPos> loopedLeavesBlocks = new HashSet<>();
			for (Direction direction : Direction.values()) {
				BlockPos neighborPos = logPos.offset(direction.getUnitVec3i());
				loopLeaves(level, neighborPos, leavesBlocks, loopedLeavesBlocks, 1);
			}
		});
		if (leavesBlocks.isEmpty()) return builder.build(false);

		Set<BlockPos> treeBlocks = new HashSet<>();
		treeBlocks.addAll(logBlocks);
		treeBlocks.addAll(leavesBlocks);

		treeBlocks.forEach(pos -> {
			for (Direction direction : Direction.values()) {
				BlockPos neighborPos = pos.offset(direction.getUnitVec3i());
				loopExtraBlocks(level, neighborPos, decorationBlocks, loopedDecorationBlocks);
			}
		});

		return builder
				.addBlocks(treeBlocks)
				.addBlocks(decorationBlocks)
				.setAwardedBlocks(logBlocks.size())
				.setFoodExhaustion(logBlocks.size())
				.setToolDamage(logBlocks.size())
				.build(true);
	}

	private void loopLogs(Level level, BlockPos blockPos, Set<BlockPos> blocks, Set<BlockPos> loopedBlocks, BlockPos startPos) throws TreeTooBigException {
		if (loopedBlocks.contains(blockPos)) return;
		loopedBlocks.add(blockPos);

		BlockState blockState = level.getBlockState(blockPos);
		if (this.isTreeStem(blockState)) {
			blocks.add(blockPos);
			if (isMaxAmountReached(blocks.size()))
				throw new TreeTooBigException(startPos, level);

			for (BlockPos offset : BlockPos.betweenClosed(new BlockPos(-1, 0, -1), new BlockPos(1, 1, 1))) {
				BlockPos neighborPos = blockPos.offset(offset);
				loopLogs(level, neighborPos, blocks, loopedBlocks, startPos);
			}
		}
	}

	private void loopLeaves(BlockGetter level, BlockPos blockPos, Set<BlockPos> blocks, Set<BlockPos> loopedBlocks, int recursionDistance) {
		if (recursionDistance > getConfig().algorithm.maxLeavesRadius) return;
		BlockState blockState = level.getBlockState(blockPos);
		if (loopedBlocks.contains(blockPos) ||
				(blockState.hasProperty(LeavesBlock.DISTANCE) && blockState.getValue(LeavesBlock.DISTANCE) != recursionDistance)) return;

		loopedBlocks.add(blockPos);

		if (this.extraRequiredBlockCheck(blockState)) {
			blocks.add(blockPos);

			for (Direction direction : Direction.values()) {
				BlockPos neighborPos = blockPos.offset(direction.getUnitVec3i());
				loopLeaves(level, neighborPos, blocks, loopedBlocks, recursionDistance + 1);
			}
		}
	}

	private void loopExtraBlocks(BlockGetter level, BlockPos blockPos, Set<BlockPos> blocks, Set<BlockPos> loopedBlocks) {
		BlockState blockState = level.getBlockState(blockPos);
		if (loopedBlocks.contains(blockPos)) return;

		loopedBlocks.add(blockPos);


		if (getConfig().extraBlockFilter.isValid(blockState)) {
			blocks.add(blockPos);

			loopExtraBlocks(level, blockPos.offset(Direction.DOWN.getUnitVec3i()), blocks, loopedBlocks);
		}
	}

	@Override
	public GenericTreeConfig getConfig() {
		return FallingTreesConfig.getCommonConfig().trees.genericTree;
	}

	public boolean isMaxAmountReached(int amount) {
		return amount >= getConfig().algorithm.maxLogAmount;
	}

	@Override
	public boolean enabled() {
		return getConfig().enabled;
	}
}
