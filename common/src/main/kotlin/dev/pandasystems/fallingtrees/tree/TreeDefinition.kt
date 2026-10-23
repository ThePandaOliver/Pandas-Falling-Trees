package dev.pandasystems.fallingtrees.tree

import dev.pandasystems.fallingtrees.config.CommonConfig
import dev.pandasystems.pandalib.core.handles.Identifier
import net.minecraft.core.BlockPos

data class TreeDefinition(
	val id: Identifier,
	val priority: Int = 0,
	val detector: TreeDetector = StandardTreeDetector,
	val enabledPredicate: (CommonConfig) -> Boolean = { true },
	val trunkMatcher: BlockMatcher,
	val leafMatcher: BlockMatcher = BlockMatcher.NONE,
	val decorationMatcher: BlockMatcher = BlockMatcher.NONE,
	val groundMatcher: BlockMatcher = DefaultGroundMatchers.STANDARD_GROUND,
	val minLeaves: Int? = null,
	val requireNaturalLeaves: Boolean = true,
	val maxTrunkBlocks: Int? = null,
	val maxLeafBlocks: Int? = null,
	val maxLeafDistance: Int? = null,
	val allowDiagonalTrunks: Boolean = true
) {
	fun isEnabled(config: CommonConfig): Boolean = enabledPredicate(config)

	fun resolveMinLeaves(config: CommonConfig): Int = minLeaves ?: config.minLeavesRequired

	fun resolveMaxTrunkBlocks(config: CommonConfig): Int = maxTrunkBlocks ?: config.maxTrunkBlocks

	fun resolveMaxLeafBlocks(config: CommonConfig): Int = maxLeafBlocks ?: config.maxLeafBlocks

	fun resolveMaxLeafDistance(config: CommonConfig): Int = maxLeafDistance ?: config.maxLeafDistance
}

data class ScannedTree(
	val origin: BlockPos,
	val definition: TreeDefinition,
	val trunkPositions: List<BlockPos>,
	val leafPositions: List<BlockPos>,
	val decorationPositions: List<BlockPos>,
	val remainingGroundAnchors: Int
) {
	val totalTrunkBlocks: Int
		get() = trunkPositions.size

	val totalBlocks: Int
		get() = trunkPositions.size + leafPositions.size + decorationPositions.size

	val isFullySeparatedFromGround: Boolean
		get() = remainingGroundAnchors == 0

	fun shouldFall(config: CommonConfig): Boolean {
		if (trunkPositions.size <= 1 && leafPositions.isEmpty()) {
			return false
		}
		if (config.requireFullGroundSeparation && !isFullySeparatedFromGround) {
			return false
		}
		return true
	}
}
