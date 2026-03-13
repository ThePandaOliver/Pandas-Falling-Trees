package dev.pandasystems.fallingtrees.trees

import com.mojang.logging.LogUtils
import dev.pandasystems.fallingtrees.FallingTrees
import dev.pandasystems.fallingtrees.api.TreeBlob
import dev.pandasystems.fallingtrees.api.TreeType
import dev.pandasystems.pandalib.config.ConfigRegistry
import dev.pandasystems.pandalib.config.syncOption
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.state.BlockState

class OverworldTree : TreeType() {
    private val logger = LogUtils.getLogger()

    override fun canPlayerFellTree(
        player: Player,
        level: Level,
        pos: BlockPos,
        state: BlockState
    ): Boolean {
        // TODO: Validate the mainHandItem with a tool filter from the trees config
        return !(config.get().requireTool && !player.mainHandItem.`is`(ItemTags.AXES))
    }

    override fun scanForTree(
        level: Level,
        pos: BlockPos,
        state: BlockState
    ): TreeBlob? {
        logger.debug("Scanning for overworld tree at $pos")
        val foundBlocks = mutableListOf<BlockPos>()
        logger.debug("Scanning for logs")
        foundBlocks.addAll(scanForLogs(level, pos))
        if (foundBlocks.isEmpty()) {
            logger.debug("No logs found")
            return null
        }

        val foundLeavesBlocks = mutableListOf<BlockPos>()
        logger.debug("Scanning for leaves")
        for (logPos in foundBlocks.toList()) {
            for (direction in Direction.entries) {
                foundLeavesBlocks.addAll(scanForLeaves(level, logPos.relative(direction)))
            }
        }
        if (foundLeavesBlocks.isEmpty()) {
            logger.debug("No leaves found")
            return null
        }
        foundBlocks.addAll(foundLeavesBlocks)

        logger.debug("Completed scanning for overworld tree at {}, found blocks:", pos)
        return TreeBlob(
            this,
            foundBlocks,
            level,
            pos,
            1f
        )
    }

    private val logScanPosOffsets = listOf(
        BlockPos(0, 0, 1),      // North
        BlockPos(0, 0, -1),     // South
        BlockPos(1, 0, 0),      // East
        BlockPos(-1, 0, 0),     // West

        // UP
        BlockPos(1, 1, 1),      // North-East
        BlockPos(0, 1, 1),      // North
        BlockPos(-1, 1, 1),     // North-West
        BlockPos(1, 1, 0),      // East
        BlockPos(0, 1, 0),      // Center
        BlockPos(-1, 1, 0),     // West
        BlockPos(1, 1, -1),     // South-East
        BlockPos(0, 1, -1),     // South
        BlockPos(-1, 1, -1),    // South-West
    )

    fun scanForLogs(level: Level, pos: BlockPos): List<BlockPos> {
        if (!isLog(level.getBlockState(pos))) return emptyList()
        val logs = mutableListOf<BlockPos>()
        val visited = mutableSetOf<BlockPos>()
        val queue = ArrayDeque<BlockPos>()
        queue.addLast(pos)

        while (queue.isNotEmpty()) {
            val currentPos = queue.removeFirst()
            if (currentPos in visited) continue
            visited.add(currentPos)
            logs.add(currentPos)
            logger.debug("Visited and added log at {}", currentPos)

            for (offset in logScanPosOffsets) {
                val next = currentPos.immutable().offset(offset)
                if (isLog(level.getBlockState(next))) {
                    logger.debug("Found log at {}", next)
                    queue.addLast(next)
                } else {
                    logger.debug("Checked block at {} and it's not a log", next)
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

            if (config.get().ignorePersistentLeaves && state.hasProperty(LeavesBlock.PERSISTENT)) {
                if (state.getValue(LeavesBlock.PERSISTENT))
                    continue
            }

            leaves.add(currentPos)
            logger.debug("Visited and added leaves at {}", currentPos)

            for (direction in Direction.entries) {
                val next = currentPos.relative(direction)
                if (isLeaves(level.getBlockState(next))) {
                    logger.debug("Found leaves at {}", next)
                    queue.addLast(LeavesData(expectedDistance + 1, next))
                } else {
                    logger.debug("Checked block at {} and it's not leaves", next)
                }
            }
        }

        return leaves.toList()
    }

    fun isLeaves(state: BlockState): Boolean {
        return state.`is`(BlockTags.LEAVES) || state.block is LeavesBlock
    }

    override fun validateTree(blob: TreeBlob): Boolean {
        TODO("Not yet implemented")
    }

    companion object {
        val config = ConfigRegistry.create(FallingTrees.identifier("trees/overworld"), Config()).apply {
            load()
            val config = get()
            syncOption(config::requireTool)
            syncOption(config::ignorePersistentLeaves)
        }
    }

    class Config {
        var requireTool: Boolean = true
        var ignorePersistentLeaves: Boolean = true
    }
}