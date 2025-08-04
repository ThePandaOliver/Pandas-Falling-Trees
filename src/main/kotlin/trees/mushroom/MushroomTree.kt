package dev.pandasystems.fallingtrees.trees.mushroom

import dev.pandasystems.fallingtrees.api.TreeData
import dev.pandasystems.fallingtrees.api.TreeType
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

class MushroomTree : TreeType {
	override fun isTreeStem(blockState: BlockState): Boolean {
		return blockState.`is`(Blocks.MUSHROOM_STEM)
	}

	override fun gatherTreeData(
		blockPos: BlockPos,
		level: Level,
		player: Player
	): TreeData? {
		var data: TreeData? = RED_MUSHROOM_TREE.gatherTreeData(blockPos, level, player)
		if (data == null) data = BROWN_MUSHROOM_TREE.gatherTreeData(blockPos, level, player)
		return data
	}

	companion object {
		val RED_MUSHROOM_TREE: RedMushroomTree = RedMushroomTree()
		val BROWN_MUSHROOM_TREE: BrownMushroomTree = BrownMushroomTree()
	}
}
