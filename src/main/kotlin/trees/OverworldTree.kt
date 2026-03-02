package dev.pandasystems.fallingtrees.trees

import dev.pandasystems.fallingtrees.api.TreeBlob
import dev.pandasystems.fallingtrees.api.TreeType
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class OverworldTree : TreeType() {
	val requireTool: Boolean by ConfigValue("require_tool", false)

	override fun scanBlocks(
		level: Level,
		pos: BlockPos,
		state: BlockState
	): TreeBlob {
		val foundBlocks = mutableListOf<BlockPos>()
		foundBlocks.addAll(scanForLogs(level, pos))

		return TreeBlob(
			this,
			foundBlocks.toList(),
			level,
			1f
		)
	}

	fun scanForLogs(level: Level, pos: BlockPos): List<BlockPos> {
		val logs = mutableListOf<BlockPos>()
		val visited = mutableSetOf<BlockPos>()
		val queue = ArrayDeque<BlockPos>()
		queue.addLast(pos)

		while (queue.isNotEmpty()) {
			val currentPos = queue.removeFirst()
			if (visited.contains(currentPos)) continue
			visited.add(currentPos)

			 for (direction in Direction.entries) {
			    val next = currentPos.relative(direction)
			    if (next !in visited && isLog(level.getBlockState(next))) {
			        visited.add(next)
			        queue.addLast(next)
			    }
			 }
		}

		return logs.toList()
	}

	fun isLog(state: BlockState): Boolean {
		return state.`is`(BlockTags.LOGS)
	}

	override fun validateTree(blob: TreeBlob): Boolean {
		TODO("Not yet implemented")
	}
}