package dev.pandasystems.fallingtrees.fabric.compat

import com.google.auto.service.AutoService
import dev.pandasystems.fallingtrees.compat.ModCompatibilities
import dev.pandasystems.fallingtrees.compat.TreeChopCompat
import dev.pandasystems.fallingtrees.compat.TreeChopCompat.Companion.tryMakeTreeFall
import ht.treechop.api.ChopData
import ht.treechop.api.TreeChopEvents
import ht.treechop.common.chop.ChopUtil
import ht.treechop.common.registry.FabricModBlocks
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

@AutoService(TreeChopCompat::class)
class TreeChopCompatImpl : TreeChopCompat {
    override fun isCoppedLog(blockState: BlockState): Boolean {
        return blockState.`is`(FabricModBlocks.CHOPPED_LOG)
    }

    override fun isChoppable(
        level: Level,
        blockPos: BlockPos
    ): Boolean {
        if (ModCompatibilities.isTreeChopLoaded)
            return ChopUtil.isBlockChoppable(level, blockPos)
        return false
    }

    companion object {
        init {
            TreeChopEvents.BEFORE_CHOP.register(::beforeFellEvent)
        }

        private fun beforeFellEvent(
            level: Level,
            player: Player,
            blockPos: BlockPos,
            blockState: BlockState,
            chopData: ChopData
        ): Boolean {
            return tryMakeTreeFall(blockPos, level, player)
        }
    }
}