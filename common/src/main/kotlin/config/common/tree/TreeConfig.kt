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
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

open class TreeConfig {
	val enabled by configOption(true)

	val requireTool by configOption(false)

	@ConfigCategory
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
		val whitelistedTags by configOption(whitelistedTags)
		val whitelist by configOption(whitelist)
		val blacklist by configOption(blacklist)

		fun isValid(blockState: BlockState): Boolean {
			val block = blockState.block
			val resourceLocation = BuiltInRegistries.BLOCK.getKey(block)
			if (blacklist.value.contains(resourceLocation.toString())) return false
			return blockState.tags.anyMatch { blockTagKey: TagKey<Block> -> whitelistedTags.value.contains(blockTagKey.location().toString()) } ||
					whitelist.value.contains(resourceLocation.toString())
		}

		fun isValid(itemStack: ItemStack): Boolean {
			val item = itemStack.item
			val resourceLocation = BuiltInRegistries.ITEM.getKey(item)
			if (blacklist.value.contains(resourceLocation.toString())) return false
			return itemStack.tags.anyMatch { blockTagKey: TagKey<Item> -> whitelistedTags.value.contains(blockTagKey.location().toString()) } ||
					whitelist.value.contains(resourceLocation.toString())
		}
	}
}
