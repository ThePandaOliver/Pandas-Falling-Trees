/*
 * Copyright (C) 2025 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package dev.pandasystems.fallingtrees.config.common.tree

import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags

class GenericTreeConfig : TreeConfig() {
	val algorithm = Algorithm()

	val logFilter = Filter(
		mutableListOf(BlockTags.LOGS.location().toString()),
		mutableListOf(),
		mutableListOf()
	)
	val leavesFilter: Filter = Filter(
		mutableListOf(BlockTags.LEAVES.location().toString()),
		mutableListOf(
//			BuiltInRegistries.BLOCK.getKey(Blocks.NETHER_WART_BLOCK).toString(),
//			BuiltInRegistries.BLOCK.getKey(Blocks.WARPED_WART_BLOCK).toString(),
//			BuiltInRegistries.BLOCK.getKey(Blocks.SHROOMLIGHT).toString(),
		),
		mutableListOf()
	)

	override val allowedToolFilter: Filter = Filter(mutableListOf(ItemTags.AXES.location().toString()), mutableListOf(), mutableListOf())

	class Algorithm {
		var maxLeavesRadius = 7
		var maxLogAmount = 256
		var shouldIgnorePersistentLeaves = true
	}
}
