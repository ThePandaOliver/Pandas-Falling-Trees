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
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.stats.Stats
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import java.util.*

class ChorusTree : TreeType {
	val config get() = fallingTreesCommonConfig.get().trees.chorusTree

	private val horizontalDirections = arrayOf(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)

	override fun isTreeStem(blockState: BlockState): Boolean {
		return isPlant(blockState)
	}

	override fun gatherTreeData(blockPos: BlockPos, level: Level, player: Player): TreeData? {
		var blockPos = blockPos
		if (this.config.requireTool.value && !this.config.allowedToolFilter.isValid(player.mainHandItem)) return null

		blockPos = blockPos.immutable()
		val builder = TreeData.Builder()

		val blockPosSet = gatherBlocks(level, blockPos, builder, player)
		return builder
			.addBlocks(blockPosSet)
			.setToolDamage(blockPosSet.size)
			.setFoodExhaustionModifier { originalExhaustion -> originalExhaustion * blockPosSet.size }
			.setMiningSpeedModifier { originalMiningSpeed ->
				val speedMultiplication: Float = fallingTreesCommonConfig.get().dynamicMiningSpeed.speedMultiplication.value
				val multiplyAmount = fallingTreesCommonConfig.get().dynamicMiningSpeed.maxSpeedMultiplication.value.coerceAtMost((blockPosSet.size.toFloat() - 1f))
				originalMiningSpeed / (multiplyAmount * speedMultiplication + 1f)
			}
			.build()
	}

	private fun gatherBlocks(level: Level, startPos: BlockPos?, builder: TreeData.Builder, player: Player): MutableSet<BlockPos> {
		val blocks = mutableSetOf<BlockPos>()
		val toVisit: Queue<BlockPos> = LinkedList()
		val visited = mutableSetOf<BlockPos>()

		toVisit.add(startPos)
		while (!toVisit.isEmpty()) {
			val current = toVisit.poll()
			if (visited.contains(current)) {
				continue
			}
			visited.add(current)

			val currentState = level.getBlockState(current)
			if (isFlower(currentState)) {
				blocks.add(current)
				continue
			}

			if (isPlant(currentState)) {
				blocks.add(current)
				builder.addAwardedStat(Stats.BLOCK_MINED.get(currentState.block))

				if (level is ServerLevel) builder.addDrops(Block.getDrops(currentState, level, current, null, player, player.mainHandItem))

				for (neighbor in gatherValidBlocksAround(level, current)) {
					if (!visited.contains(neighbor)) {
						toVisit.add(neighbor)
					}
				}
			}
		}
		return blocks
	}

	private fun isPlant(blockState: BlockState): Boolean {
		return blockState.`is`(Blocks.CHORUS_PLANT)
	}

	private fun isFlower(blockState: BlockState): Boolean {
		return blockState.`is`(Blocks.CHORUS_FLOWER)
	}

	private fun gatherValidBlocksAround(level: Level, blockPos: BlockPos): MutableList<BlockPos> {
		val blocks = mutableListOf<BlockPos>()
		for (direction in horizontalDirections) {
			val neighborPos = blockPos.relative(direction)
			if (isPlant(level.getBlockState(neighborPos.below()))) continue
			val blockState = level.getBlockState(neighborPos)
			if (isPlant(blockState) || isFlower(blockState)) blocks.add(neighborPos)
		}
		val neighborPos = blockPos.above()
		val blockState = level.getBlockState(neighborPos)
		if (isPlant(blockState) || isFlower(blockState)) blocks.add(neighborPos)
		return blocks
	}
}
