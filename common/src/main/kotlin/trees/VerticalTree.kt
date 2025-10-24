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
package dev.pandasystems.fallingtrees.trees

import dev.pandasystems.fallingtrees.api.TreeData
import dev.pandasystems.fallingtrees.api.TreeType
import dev.pandasystems.fallingtrees.config.fallingTreesCommonConfig
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.stats.Stats
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

class VerticalTree : TreeType {
	val config get() = fallingTreesCommonConfig.get().trees.verticalTree

	override fun isTreeStem(blockState: BlockState): Boolean {
		return this.config.filter.isValid(blockState)
	}

	override fun gatherTreeData(blockPos: BlockPos, level: Level, player: Player): TreeData? {
		var blockPos = blockPos
		if (this.config.requireTool.value && !this.config.allowedToolFilter.isValid(player.mainHandItem)) return null

		blockPos = blockPos.immutable()
		val builder = TreeData.Builder()

		val blocks = mutableListOf<BlockPos>()
		gatherBlocks(level, blockPos, blocks)

		val drops = mutableListOf<ItemStack>()
		if (level is ServerLevel) {
			for (block in blocks) {
				val blockState = level.getBlockState(block)
				val items = Block.getDrops(blockState, level, block, null, player, player.mainHandItem)
				drops.addAll(items)
			}
		}

		return builder
			.addBlocks(blocks)
			.setToolDamage(blocks.size)
			.setFoodExhaustionModifier { originalExhaustion: Float -> originalExhaustion * blocks.size }
			.addDrops(drops)
			.setMiningSpeedModifier { originalMiningSpeed: Float ->
				val speedMultiplication: Float = fallingTreesCommonConfig.get().dynamicMiningSpeed.speedMultiplication.value
				val multiplyAmount = fallingTreesCommonConfig.get().dynamicMiningSpeed.maxSpeedMultiplication.value.coerceAtMost((blocks.size.toFloat() - 1f))
				originalMiningSpeed / (multiplyAmount * speedMultiplication + 1f)
			}
			.addAwardedStats(blocks.stream().map { logPos: BlockPos ->
				val blockState = level.getBlockState(logPos)
				Stats.BLOCK_MINED.get(blockState.block)
			}.toList())
			.build()
	}

	private fun gatherBlocks(level: Level, blockPos: BlockPos, blocks: MutableList<BlockPos>) {
		val blockState = level.getBlockState(blockPos)
		blocks.add(blockPos)

		val neighborPos = blockPos.above()
		if (level.getBlockState(neighborPos).`is`(blockState.block)) gatherBlocks(level, neighborPos, blocks)
	}
}
