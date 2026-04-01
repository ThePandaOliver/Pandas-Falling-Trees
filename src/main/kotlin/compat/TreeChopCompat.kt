package dev.pandasystems.fallingtrees.compat

import dev.pandasystems.fallingtrees.api.TreeHandler
import dev.pandasystems.pandalib.utils.loadFirstService
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

interface TreeChopCompat {
    fun isCoppedLog(blockState: BlockState): Boolean
    fun isChoppable(level: Level, blockPos: BlockPos): Boolean

    companion object {
        val compat: TreeChopCompat = loadFirstService<TreeChopCompat>()

        fun tryMakeTreeFall(blockPos: BlockPos, level: Level, player: Player): Boolean {
            if (compat.isCoppedLog(level.getBlockState(blockPos)))
                return tryMakeTreeFall(blockPos.above(), level, player)
            return TreeHandler.destroyTree(level, blockPos, player)
        }

        fun isChoppable(level: Level, blockPos: BlockPos): Boolean {
            return compat.isChoppable(level, blockPos)
        }
    }
}