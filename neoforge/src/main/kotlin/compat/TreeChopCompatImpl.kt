package dev.pandasystems.fallingtrees.neoforge.compat

import com.google.auto.service.AutoService
import dev.pandasystems.fallingtrees.compat.ModCompatibilities
import dev.pandasystems.fallingtrees.compat.TreeChopCompat
import ht.treechop.api.ChopEvent
import ht.treechop.common.NeoForgeRegistry
import ht.treechop.common.chop.ChopUtil
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.bus.api.IEventBus

@AutoService(TreeChopCompat::class)
class TreeChopCompatImpl : TreeChopCompat {
    override fun isCoppedLog(blockState: BlockState): Boolean {
        return blockState.`is`(NeoForgeRegistry.Blocks.CHOPPED_LOG)
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
        fun init(eventBus: IEventBus) {
            eventBus.addListener(::beforeFellEvent)
        }

        private fun beforeFellEvent(event: ChopEvent.BeforeFellEvent) {
            val player = event.player
            if (player !is ServerPlayer) return
            if (TreeChopCompat.tryMakeTreeFall(event.choppedBlockPos, event.level, player))
                event.isCanceled = true
        }
    }
}