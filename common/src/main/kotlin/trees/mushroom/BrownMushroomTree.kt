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

package dev.pandasystems.fallingtrees.trees.mushroom

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
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import java.util.LinkedList
import java.util.Queue
import java.util.Stack
import java.util.function.Consumer

class BrownMushroomTree : TreeType {
	private val capScanOffset = arrayOf(
		BlockPos(-1, 0, 0), BlockPos(1, 0, 0),
		BlockPos(0, 0, -1), BlockPos(0, 0, 1),
	)

	val config get() = fallingTreesCommonConfig.config.trees.mushroomTree

	override fun isTreeStem(blockState: BlockState): Boolean {
		return blockState.`is`(Blocks.MUSHROOM_STEM)
	}

	override fun gatherTreeData(blockPos: BlockPos, level: Level, player: Player): TreeData? {
		var blockPos = blockPos
		if (this.config.requireTool && !this.config.allowedToolFilter.isValid(player.mainHandItem)) return null

		blockPos = blockPos.immutable()
		val builder: TreeData.Builder = TreeData.Builder()

		val stemBlocks = gatherStemBlocks(level, blockPos)
		val capBlocks = mutableSetOf<BlockPos>()

		stemBlocks.forEach(Consumer { stemPos: BlockPos -> capBlocks.addAll(gatherCapBlocks(level, stemPos.above())) })
		if (capBlocks.isEmpty()) return null

		val blocks = mutableListOf<BlockPos>()
		blocks.addAll(stemBlocks)
		blocks.addAll(capBlocks)

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
			.setFoodExhaustionModifier { originalExhaustion -> originalExhaustion * blocks.size }
			.addDrops(drops)
			.setMiningSpeedModifier { originalMiningSpeed ->
				val speedMultiplication: Float = fallingTreesCommonConfig.config.dynamicMiningSpeed.speedMultiplication
				val multiplyAmount = fallingTreesCommonConfig.config.dynamicMiningSpeed.maxSpeedMultiplication.coerceAtMost((blocks.size.toFloat() - 1f))
				originalMiningSpeed / (multiplyAmount * speedMultiplication + 1f)
			}
			.addAwardedStats(blocks.stream().map { logPos: BlockPos ->
				val blockState = level.getBlockState(logPos)
				Stats.BLOCK_MINED.get(blockState.block)
			}.toList())
			.build()
	}

	private fun gatherStemBlocks(level: Level, startPos: BlockPos): MutableSet<BlockPos> {
		val blocks = mutableSetOf<BlockPos>()
		val toVisit = Stack<BlockPos>()
		val visited = mutableSetOf<BlockPos>()

		toVisit.add(startPos)
		while (!toVisit.isEmpty()) {
			val current = toVisit.pop()
			if (visited.contains(current)) {
				continue
			}
			visited.add(current)

			val currentState = level.getBlockState(current)
			if (isTreeStem(currentState)) {
				blocks.add(current)

				val neighbor = current.above()
				if (!visited.contains(neighbor)) {
					toVisit.add(neighbor)
				}
			}
		}
		return blocks
	}

	private fun gatherCapBlocks(level: Level, startPos: BlockPos): MutableSet<BlockPos> {
		val blocks = mutableSetOf<BlockPos>()
		val toVisit: Queue<BlockSearchNode> = LinkedList()
		val visited = mutableSetOf<BlockPos>()

		toVisit.add(BlockSearchNode(startPos, 1))
		while (!toVisit.isEmpty()) {
			val node = toVisit.poll()
			val current = node.position

			if (visited.contains(current) || node.distance > 6) {
				continue
			}
			visited.add(current)

			val currentState = level.getBlockState(current)
			if (currentState.`is`(Blocks.BROWN_MUSHROOM_BLOCK)) {
				blocks.add(current)

				for (offset in capScanOffset) {
					val neighbor = current.offset(offset)
					if (!visited.contains(neighbor)) {
						toVisit.add(BlockSearchNode(neighbor, node.distance + 1))
					}
				}
			}
		}
		return blocks
	}

	@JvmRecord
	private data class BlockSearchNode(val position: BlockPos, val distance: Int)
}
