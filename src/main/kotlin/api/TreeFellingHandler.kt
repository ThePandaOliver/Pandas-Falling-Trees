package dev.pandasystems.fallingtrees.api

import dev.pandasystems.fallingtrees.treeRegistry
import dev.pandasystems.pandalib.event.server.serverBlockBreakPreEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

object TreeFellingHandler {
    private val cachedTrees = mutableListOf<TreeBlob>() // TODO: Add proper cache cleanup
    
    internal fun init() {
        serverBlockBreakPreEvent.register { level, pos, _, player ->
            if (level !is ServerLevel) return@register false
            val treeBlob = scanTree(level, pos, player, false) ?: return@register false
            return@register fellTree(treeBlob, player)
        }
    }
    
    fun scanTree(level: ServerLevel, pos: BlockPos, player: ServerPlayer, useCache: Boolean = true): TreeBlob? {
        var treeBlob = if (useCache) getCachedTree(pos) else null
        
        if (treeBlob != null) {
            if (treeBlob.validate())
                return treeBlob
            else
                removeCachedTree(treeBlob)
        }
        
        treeBlob = treeRegistry.firstNotNullOfOrNull { tree ->
            if (tree.enabled && tree.canPlayerFellTree(player, level, pos))
                tree.scanForTree(level, pos)
            else null
        } ?: return null

        addCachedTree(treeBlob)
        return treeBlob
    }

    fun fellTree(treeBlob: TreeBlob, player: ServerPlayer): Boolean {
        val level = treeBlob.level
        val pos = treeBlob.originBlockPos
        
        // The first loop is performed to destroy the blocks
        treeBlob.blockPoses.forEach { blockPos -> 
            level.destroyBlock(blockPos, false, player, 0)
        }
        
        // The second loop is performed to update neighboring blocks
        treeBlob.blockPoses.forEach { blockPos ->
            val blockState = level.getBlockState(blockPos)
            
            // The flags are based on the source code of the Level.destroyBlock method, their use is unknown though.
            val flags = 3 and -34
            
            // The recursion amount starts at 511 instead of 512, because the first destruction already counted as 1
            val recursionAmount = 511
            
            blockState.updateNeighbourShapes(level, pos, flags, recursionAmount)
            blockState.updateIndirectNeighbourShapes(level, pos, flags, recursionAmount)
        }
        
        removeCachedTree(treeBlob)
        return true
    }
    
    fun addCachedTree(tree: TreeBlob) {
        cachedTrees.add(tree)
    }
    
    fun removeCachedTree(treeBlob: TreeBlob): Boolean {
        return cachedTrees.remove(treeBlob)
    }
    
    fun getCachedTree(blockPos: BlockPos): TreeBlob? {
        return cachedTrees.firstOrNull { it.blockPoses.contains(blockPos) }
    }
}