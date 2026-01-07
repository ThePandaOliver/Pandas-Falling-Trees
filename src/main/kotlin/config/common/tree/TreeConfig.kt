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

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

open class TreeConfig {
	var enabled = true

	var requireTool = false

	open val allowedToolFilter: Filter = Filter(
		mutableListOf(),
		mutableListOf(),
		mutableListOf()
	)

	class Filter(
		whitelistedTags: MutableList<String> = mutableListOf(),
		whitelist: MutableList<String> = mutableListOf(),
		blacklist: MutableList<String> = mutableListOf()
	) {
		var whitelistedTags = whitelistedTags
		var whitelist = whitelist
		var blacklist = blacklist

		fun isValid(blockState: BlockState): Boolean {
			val block = blockState.block
			val resourceLocation = BuiltInRegistries.BLOCK.getKey(block)
			if (blacklist.contains(resourceLocation.toString())) return false
			return blockState.tags.anyMatch { blockTagKey: TagKey<Block> -> whitelistedTags.contains(blockTagKey.location().toString()) } ||
					whitelist.contains(resourceLocation.toString())
		}

		fun isValid(itemStack: ItemStack): Boolean {
			val item = itemStack.item
			val resourceLocation = BuiltInRegistries.ITEM.getKey(item)
			if (blacklist.contains(resourceLocation.toString())) return false
			return itemStack.tags.anyMatch { blockTagKey: TagKey<Item> -> whitelistedTags.contains(blockTagKey.location().toString()) } ||
					whitelist.contains(resourceLocation.toString())
		}
	}
}
