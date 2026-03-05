package dev.pandasystems.fallingtrees.api

import com.mojang.logging.LogUtils
import dev.pandasystems.fallingtrees.entity.TreeEntity
import dev.pandasystems.fallingtrees.treeRegistry
import dev.pandasystems.pandalib.event.server.serverBlockBreakPreEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

object TreeFellingHandler {
	private val logger = LogUtils.getLogger()
	private val cachedTrees = mutableListOf<TreeBlob>() // TODO: Add proper cache cleanup

	internal fun init() {
		serverBlockBreakPreEvent.register { level, pos, _, player ->
			if (level !is ServerLevel) return@register true
			val treeBlob = scanTree(level, pos, player, false) ?: return@register true
			return@register !fellTree(treeBlob, player)
		}
	}

	fun scanTree(level: ServerLevel, pos: BlockPos, player: ServerPlayer, useCache: Boolean = true): TreeBlob? {
		logger.debug("Scanning for tree at {}", pos)
		var treeBlob = if (useCache) getCachedTree(pos) else null

		if (treeBlob != null) {
			logger.debug("Found cached tree at {}", pos)
			if (treeBlob.validate()) {
				logger.debug("Tree at {} is valid", pos)
				return treeBlob
			} else {
				logger.debug("Tree at {} is invalid, so it's removed from the cache", pos)
				removeCachedTree(treeBlob)
			}
		}

		treeBlob = treeRegistry.firstNotNullOfOrNull { tree ->
			val treeIdentifier = treeRegistry.getKey(tree)
			logger.debug("Checking if tree {} is enabled and can be fell by player", treeIdentifier)
			if (tree.enabled && tree.canPlayerFellTree(player, level, pos)) {
				logger.debug("Scanning for tree at {} using tree {}", pos, treeIdentifier)
				val treeBlob = tree.scanForTree(level, pos)
				if (treeBlob == null) logger.debug("Tree at {} could not be found using tree {}", pos, treeIdentifier)
				treeBlob
			} else null
		} ?: return null

		addCachedTree(treeBlob)
		logger.debug("Tree at {} is valid, so it's added to the cache", pos)
		return treeBlob
	}

	fun fellTree(treeBlob: TreeBlob, player: ServerPlayer): Boolean {
		logger.debug("Felling tree at {}", treeBlob.originBlockPos)
		val level = treeBlob.level
		val originBlockPos = treeBlob.originBlockPos

		data class BlockRemovalData(val pos: BlockPos, val oldState: BlockState, val newState: BlockState)

		treeBlob.blockStates.keys.map { blockPos -> // First loop we remove the blocks without updating neighbors
			val newState = Blocks.AIR.defaultBlockState()
			val oldState = level.getBlockState(blockPos)
			level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3, 0)
			BlockRemovalData(blockPos, oldState, newState) // This data is used in the second loop
		}.let { dataList ->
			logger.debug("Removed blocks but not updating neighbors")
			dataList
		}.forEach { (pos, oldState, newState) -> // Second loop we update the neighbors
			val flags = 3 and -34
			oldState.updateIndirectNeighbourShapes(level, pos, flags, 511)
			newState.updateNeighbourShapes(level, pos, flags, 511)
			newState.updateIndirectNeighbourShapes(level, pos, flags, 511);
		}
		logger.debug("Updated neighbors")

		removeCachedTree(treeBlob)
		logger.debug("Removed tree from cache")

		val treeEntity = TreeEntity(level = level)
		treeEntity.setPos(originBlockPos.x.toDouble() + .5, originBlockPos.y.toDouble(), originBlockPos.z.toDouble() + .5)
		treeEntity.setData(treeBlob, player)
		level.addFreshEntity(treeEntity)
		logger.debug("Tree entity was spawned")
		return true
	}

	fun addCachedTree(tree: TreeBlob) {
		cachedTrees.add(tree)
	}

	fun removeCachedTree(treeBlob: TreeBlob): Boolean {
		return cachedTrees.remove(treeBlob)
	}

	fun getCachedTree(blockPos: BlockPos): TreeBlob? {
		return cachedTrees.firstOrNull { it.blockStates.keys.contains(blockPos) }
	}
}