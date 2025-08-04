/*
 * Copyright (C) 2024 Oliver Froberg (The Panda Oliver)
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
import java.util.List

class GenericTreeConfig : TreeConfig() {
	var algorithm = Algorithm()

	var logFilter = Filter(
		mutableListOf(BlockTags.LOGS.location().toString()),
		mutableListOf(),
		mutableListOf()
	)
	var leavesFilter: Filter = Filter(
		mutableListOf(BlockTags.LEAVES.location().toString()),
		mutableListOf(),
		mutableListOf()
	)

	init {
		this.allowedToolFilter.whitelistedTags.add(ItemTags.AXES.location().toString())
	}

	class Algorithm {
		var maxLeavesRadius: Int = 7
		var maxLogAmount: Int = 256
		var shouldIgnorePersistentLeaves: Boolean = true
	}
}
