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

import dev.pandasystems.pandalib.config.ConfigCategory
import dev.pandasystems.pandalib.config.options.configOption
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import java.util.List

class GenericTreeConfig : TreeConfig() {
	@ConfigCategory
	val algorithm = Algorithm()

	@ConfigCategory
	val logFilter = Filter(
		mutableListOf(BlockTags.LOGS.location().toString()),
		mutableListOf(),
		mutableListOf()
	)
	@ConfigCategory
	val leavesFilter: Filter = Filter(
		mutableListOf(BlockTags.LEAVES.location().toString()),
		mutableListOf(),
		mutableListOf()
	)

	override val allowedToolFilter: Filter = Filter(mutableListOf(ItemTags.AXES.location().toString()), mutableListOf(), mutableListOf())

	class Algorithm {
		val maxLeavesRadius by configOption(7)
		val maxLogAmount by configOption(256)
		val shouldIgnorePersistentLeaves by configOption(true)
	}
}
