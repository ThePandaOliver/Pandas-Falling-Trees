package dev.pandasystems.fallingtrees.tree

import dev.pandasystems.fallingtrees.config.CommonConfig
import dev.pandasystems.pandalib.core.handles.Identifier
import net.minecraft.core.BlockPos

data class TreeDefinition(
	val id: Identifier,
	val detector: Identifier = TreeDetectors.BRANCHING_ID,
	val logs: BlockMatcher,
	val leaves: BlockMatcher = BlockMatcher.NONE,
	val decorations: BlockMatcher = BlockMatcher.NONE
) {
	val hasLeaves: Boolean
		get() = leaves !== BlockMatcher.NONE
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
