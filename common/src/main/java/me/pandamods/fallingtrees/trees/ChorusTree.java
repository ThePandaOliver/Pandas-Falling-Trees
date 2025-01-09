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

import me.pandamods.fallingtrees.api.Tree;
import me.pandamods.fallingtrees.api.TreeData;
import me.pandamods.fallingtrees.api.TreeDataBuilder;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.config.common.tree.TreeConfig;
import me.pandamods.fallingtrees.entity.TreeEntity;
import me.pandamods.fallingtrees.utils.CollectionsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChorusTree implements Tree<TreeConfig> {
	@Override
	public boolean isTreeStem(BlockState blockState) {
		return blockState.is(Blocks.CHORUS_PLANT);
	}

	public boolean extraRequiredBlockCheck(BlockState blockState) {
		return blockState.is(Blocks.CHORUS_FLOWER);
	}

	@Override
	public TreeData getTreeData(TreeDataBuilder builder, BlockPos blockPos, BlockGetter level) {
		Set<BlockPos> loopedBlocks = new HashSet<>();

		loopBlocks(level, blockPos, builder, loopedBlocks);
		return builder
				.setAwardedBlocks(builder.getBlocks().size())
				.setFoodExhaustion(builder.getBlocks().size())
				.setToolDamage(builder.getBlocks().size())
				.build(true);
	}

	public void loopBlocks(BlockGetter level, BlockPos originPos, TreeDataBuilder builder, Set<BlockPos> loopedBlocks) {
		if (loopedBlocks.contains(originPos))
			return;

		loopedBlocks.add(originPos);

		BlockState blockState = level.getBlockState(originPos);
		if (this.isTreeStem(blockState) || this.extraRequiredBlockCheck(blockState)) {
			builder.addBlock(originPos);

			if (this.isTreeStem(blockState)) {
				for (Direction direction : Direction.values()) {
					if (blockState.getValue(ChorusPlantBlock.PROPERTY_BY_DIRECTION.get(direction))) {
						BlockPos neighborPos = originPos.offset(direction.getUnitVec3i());
						loopBlocks(level, neighborPos, builder, loopedBlocks);
					}
				}
			}
		}
	}

	@Override
	public float fallAnimationEdgeDistance() {
		return 6f / 16f;
	}

	@Override
	public TreeConfig getConfig() {
		return FallingTreesConfig.getCommonConfig().trees.chorusTree;
	}

	@Override
	public boolean enabled() {
		return getConfig().enabled;
	}

	@Override
	public List<ItemStack> getDrops(TreeEntity entity, Map<BlockPos, BlockState> blocks) {
		return Tree.super.getDrops(entity, CollectionsUtils.mapRemoveIf(blocks, (blockPos, blockState) -> blockState.is(Blocks.CHORUS_FLOWER)));
	}
}
