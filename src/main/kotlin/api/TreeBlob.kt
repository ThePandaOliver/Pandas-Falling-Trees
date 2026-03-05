package dev.pandasystems.fallingtrees.api

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

data class TreeBlob(
    val treeType: TreeType,
    val blockPoses: List<BlockPos>,
    val level: Level,
    val originBlockPos: BlockPos,
    val miningSpeed: Float
) {
    val blockStates = blockPoses.associateWith { level.getBlockState(it) }
    
    fun validate() = treeType.validateTree(this)
}