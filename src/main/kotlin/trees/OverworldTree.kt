package dev.pandasystems.fallingtrees.trees

import dev.pandasystems.fallingtrees.api.TreeBlob
import dev.pandasystems.fallingtrees.api.TreeType
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.state.BlockState

class OverworldTree : TreeType() {
    val requireTool: Boolean = false
    val ignorePersistentLeaves: Boolean = true

    override fun canPlayerFellTree(
        player: Player,
        level: Level,
        pos: BlockPos,
        state: BlockState
    ): Boolean {
        // TODO: Validate the mainHandItem with a tool filter from the trees config
        return !(requireTool && !player.mainHandItem.`is`(ItemTags.AXES))
    }

    override fun scanForTree(
        level: Level,
        pos: BlockPos,
        state: BlockState
    ): TreeBlob? {
        val foundBlocks = mutableListOf<BlockPos>()
        foundBlocks.addAll(scanForLogs(level, pos))
        if (foundBlocks.isEmpty()) return null

        val foundLeavesBlocks = mutableListOf<BlockPos>()
        for (log in foundBlocks.toList()) {
            for (direction in Direction.entries) {
                foundLeavesBlocks.addAll(scanForLeaves(level, log.relative(direction)))
            }
        }
        if (foundLeavesBlocks.isEmpty()) return null
        foundBlocks.addAll(foundLeavesBlocks)

        return TreeBlob(
            this,
            foundBlocks.toList(),
            level,
            pos,
            1f
        )
    }

    private val logScanPosOffsets = listOf(
        BlockPos(0, 0, 0), BlockPos(0, 0, 1),
        BlockPos(0, 0, -1), BlockPos(1, 0, 0), BlockPos(-1, 0, 0)
    ) + BlockPos.betweenClosed(BlockPos(-1, 1, -1), BlockPos(1, 1, 1))

    fun scanForLogs(level: Level, pos: BlockPos): List<BlockPos> {
        if (!isLog(level.getBlockState(pos))) return emptyList()
        val logs = mutableListOf<BlockPos>()
        val visited = mutableSetOf<BlockPos>()
        val queue = ArrayDeque<BlockPos>()
        queue.addLast(pos)

        while (queue.isNotEmpty()) {
            val currentPos = queue.removeFirst()
            if (visited.contains(currentPos)) continue
            visited.add(currentPos)
            logs.add(currentPos)

            for (offset in logScanPosOffsets) {
                val next = currentPos.offset(offset)
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

    fun scanForLeaves(level: Level, pos: BlockPos): List<BlockPos> {
        if (!isLeaves(level.getBlockState(pos))) return emptyList()
        data class LeavesData(val distance: Int, val pos: BlockPos)

        val leaves = mutableListOf<BlockPos>()
        val visited = mutableSetOf<BlockPos>()
        val queue = ArrayDeque<LeavesData>()
        queue.addLast(LeavesData(1, pos))

        while (queue.isNotEmpty()) {
            val currentLeaves = queue.removeFirst()
            val currentPos = currentLeaves.pos
            val expectedDistance = currentLeaves.distance
            if (visited.contains(currentPos)) continue
            visited.add(currentPos)

            val state = level.getBlockState(currentPos)
            // If the block has a distance property, then check if the expected distance matches, if not, skip
            if (state.hasProperty(LeavesBlock.DISTANCE)) {
                val currentDistance = state.getValue(LeavesBlock.DISTANCE)
                if (currentDistance != expectedDistance)
                    continue
            }

            if (ignorePersistentLeaves && state.hasProperty(LeavesBlock.PERSISTENT)) {
                if (state.getValue(LeavesBlock.PERSISTENT))
                    continue
            }

            leaves.add(currentPos)

            for (direction in Direction.entries) {
                val next = currentPos.relative(direction)
                if (next !in visited && isLeaves(level.getBlockState(next))) {
                    visited.add(next)
                    queue.addLast(LeavesData(expectedDistance + 1, next))
                }
            }
        }

        return leaves.toList()
    }

    fun isLeaves(state: BlockState): Boolean {
        return state.`is`(BlockTags.LEAVES)
    }

    override fun validateTree(blob: TreeBlob): Boolean {
        TODO("Not yet implemented")
    }
}