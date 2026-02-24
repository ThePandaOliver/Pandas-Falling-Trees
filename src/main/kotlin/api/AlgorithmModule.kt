package dev.pandasystems.fallingtrees.api

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

abstract class AlgorithmModule {
    abstract fun scan(level: Level, blockPos: BlockPos, blockState: BlockState = level.getBlockState(blockPos))
    abstract fun validateBlock(level: Level, blockPos: BlockPos, blockState: BlockState = level.getBlockState(blockPos))
}