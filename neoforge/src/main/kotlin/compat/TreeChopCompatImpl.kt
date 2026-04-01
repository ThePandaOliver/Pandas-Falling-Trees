package dev.pandasystems.fallingtrees.neoforge.compat

import com.google.auto.service.AutoService
import dev.pandasystems.fallingtrees.compat.TreeChopCompat
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

@AutoService(TreeChopCompat::class)
class TreeChopCompatImpl : TreeChopCompat {
    override fun isCoppedLog(blockState: BlockState): Boolean {
        return false
    }

    override fun isChoppable(
        level: Level,
        blockPos: BlockPos
    ): Boolean {
        return false
    }
}