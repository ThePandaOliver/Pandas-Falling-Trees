/*
 * Copyright (C) 2026 Oliver Froberg (The Panda Oliver)
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
import dev.pandasystems.fallingtrees.config.fallingTreesClientConfig
import dev.pandasystems.fallingtrees.config.fallingTreesCommonConfig
import dev.pandasystems.fallingtrees.entity.TreeEntity
import dev.pandasystems.fallingtrees.exceptions.TreeTooBigException
import dev.pandasystems.fallingtrees.treeFallSound
import dev.pandasystems.fallingtrees.treeImpactSound
import dev.pandasystems.pandalib.utils.gameEnvironment
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import java.util.*

class GenericTree : TreeType {
	val config get() = fallingTreesCommonConfig.get().trees.genericTree

	override fun isTreeStem(blockState: BlockState): Boolean {
		return this.config.logFilter.isValid(blockState)
	}

	override fun onTreeTick(entity: TreeEntity) {
		if (gameEnvironment.isClient) {
			val clientConfig = fallingTreesClientConfig.get()
			if (entity.tickCount == 1) {
				if (clientConfig.soundSettings.enabled) {
					entity.level().playLocalSound(
						entity.x, entity.y, entity.z, treeFallSound.get(),
						SoundSource.BLOCKS, clientConfig.soundSettings.startVolume, 1f, true
					)
				}
			}

			if (entity.tickCount == (clientConfig.animation.fallAnimLength * 20).toInt() - 5) {
				if (clientConfig.soundSettings.enabled) {
					entity.level().playLocalSound(
						entity.x, entity.y, entity.z, treeImpactSound.get(),
						SoundSource.BLOCKS, clientConfig.soundSettings.endVolume, 1f, true
					)
				}
			}
		}
	}

	override fun gatherTreeData(blockPos: BlockPos, level: Level, player: Player): TreeData? {
		var blockPos = blockPos
		if (this.config.requireTool && !this.config.allowedToolFilter.isValid(player.mainHandItem)) return null

		blockPos = blockPos.immutable()
		val builder = TreeData.Builder()

		if (!isLogBlock(level.getBlockState(blockPos))) {
			return null
		}

		val logs = gatherLogs(level, blockPos)
		if (logs.isEmpty()) {
			return null
		}

		val leaves = mutableSetOf<BlockPos>()
		for (logPos in logs) {
			leaves.addAll(gatherLeavesAroundLog(level, logPos))
		}
		if (leaves.isEmpty()) {
			return null
		}

		val adjacent = gatherAdjacentBlocks(level, logs, leaves)

		val allBlocks = logs.toMutableSet()
		allBlocks.addAll(leaves)
		allBlocks.addAll(adjacent)

		val drops = mutableListOf<ItemStack>()
		for (block in allBlocks) {
			val blockState = level.getBlockState(block)
			if (level is ServerLevel) {
				val items = Block.getDrops(blockState, level, block, null, player, player.mainHandItem)
				drops.addAll(items)
			}
		}

		println(logs)
		return builder
			.addBlocks(allBlocks)
			.setToolDamage(logs.size)
			.setFoodExhaustionModifier { originalExhaustion -> originalExhaustion * logs.size }
			.addDrops(drops)
			.setMiningSpeedModifier { originalMiningSpeed ->
				val speedMultiplication: Float = fallingTreesCommonConfig.get().dynamicMiningSpeed.speedMultiplication
				val multiplyAmount = fallingTreesCommonConfig.get().dynamicMiningSpeed.maxSpeedMultiplication.coerceAtMost((logs.size.toFloat() - 1f))
				originalMiningSpeed / (multiplyAmount * speedMultiplication + 1f)
			}
			.addAwardedStats(logs.stream().map { logPos: BlockPos ->
				val blockState = level.getBlockState(logPos)
				Stats.BLOCK_MINED.get(blockState.block)
			}.toList())
			.build()
	}

	private fun gatherLogs(level: Level, startPos: BlockPos?): MutableSet<BlockPos> {
		val logs = mutableSetOf<BlockPos>()
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
			if (isLogBlock(currentState)) {
				logs.add(current)

				if (logs.size > this.config.algorithm.maxLogAmount) {
					throw TreeTooBigException(current, level)
				}

				for (offset in BlockPos.betweenClosed(-1, 0, -1, 1, 1, 1)) {
					val neighbor = current.offset(offset)
					if (!visited.contains(neighbor)) {
						toVisit.add(neighbor)
					}
				}
			}
		}
		return logs
	}

	private fun gatherLeavesAroundLog(level: Level, logPos: BlockPos): MutableSet<BlockPos> {
		val leaves = mutableSetOf<BlockPos>()
		val toVisit: Queue<BlockSearchNode> = LinkedList()
		val visited = mutableSetOf<BlockPos>()

		for (direction in Direction.entries) {
			val neighbor = logPos.relative(direction)
			toVisit.add(BlockSearchNode(neighbor, 1))
		}

		while (!toVisit.isEmpty()) {
			val node = toVisit.poll()
			val current = node.position

			val currentState = level.getBlockState(current)
			val optionalDistanceAt = LeavesBlock.getOptionalDistanceAt(currentState)
			if (optionalDistanceAt.isPresent && node.distance != optionalDistanceAt.getAsInt()) continue
			if (visited.contains(current) || node.distance > this.config.algorithm.maxLeavesRadius) {
				continue
			}
			visited.add(current)

			if (isLeafBlock(currentState)) {
				leaves.add(current)

				for (direction in Direction.entries) {
					val nextPos = current.relative(direction)
					if (!visited.contains(nextPos)) {
						toVisit.add(BlockSearchNode(nextPos, node.distance + 1))
					}
				}
			}
		}

		return leaves
	}

	private fun gatherAdjacentBlocks(level: Level, logs: MutableSet<BlockPos>, leaves: MutableSet<BlockPos>): MutableSet<BlockPos> {
		val adjacentBlocks = mutableSetOf<BlockPos>()
		val allTreeBlocks = mutableSetOf<BlockPos>()
			.also { it.addAll(logs) }
			.also { it.addAll(leaves) }

		for (blockPos in allTreeBlocks) {
			for (dir in Direction.entries) {
				val neighbor = blockPos.relative(dir)
				val neighborState = level.getBlockState(neighbor)
				if (neighborState.`is`(Blocks.VINE)) {
					adjacentBlocks.addAll(gatherVines(level, neighbor))
				} else if (neighborState.`is`(Blocks.BEE_NEST)) {
					adjacentBlocks.add(neighbor)
				} else if (neighborState.`is`(Blocks.COCOA)) {
					adjacentBlocks.add(neighbor)
				}
			}
		}
		return adjacentBlocks
	}

	private fun gatherVines(level: Level, startPos: BlockPos): MutableSet<BlockPos> {
		val vines = mutableSetOf<BlockPos>()
		val toVisit = Stack<BlockPos>()
		val visited = mutableSetOf<BlockPos>()

		toVisit.push(startPos)
		while (!toVisit.isEmpty()) {
			val current = toVisit.pop()
			if (visited.contains(current)) {
				continue
			}
			visited.add(current)

			val currentState = level.getBlockState(current)
			if (currentState.`is`(Blocks.VINE)) {
				vines.add(current)

				val neighbor = current.below()
				if (!visited.contains(neighbor)) {
					toVisit.push(neighbor)
				}
			}
		}
		return vines
	}

	private fun isLogBlock(blockState: BlockState): Boolean {
		return this.config.logFilter.isValid(blockState)
	}

	private fun isLeafBlock(blockState: BlockState): Boolean {
		if (this.config.algorithm.shouldIgnorePersistentLeaves &&
			blockState.hasProperty(BlockStateProperties.PERSISTENT) && blockState.getValue(BlockStateProperties.PERSISTENT)
		) return false
		return this.config.leavesFilter.isValid(blockState)
	}

	@JvmRecord
	private data class BlockSearchNode(val position: BlockPos, val distance: Int)
}
