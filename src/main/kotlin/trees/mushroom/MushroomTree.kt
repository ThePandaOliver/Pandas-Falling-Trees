/*
 * Copyright (C) 2026 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
